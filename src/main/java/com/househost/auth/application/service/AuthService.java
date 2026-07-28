package com.househost.auth.application.service;

import com.househost.auth.application.dto.*;
import com.househost.auth.application.records.*;
import com.househost.auth.application.port.in.AuthUseCase;
import com.househost.auth.application.port.out.AuthAuditPort;
import com.househost.auth.application.port.out.LoginSecurityAlertPort;
import com.househost.auth.application.port.out.UserPersistencePort;
import com.househost.auth.application.port.out.PasswordPort;
import com.househost.auth.domain.exception.LoginProtectionUnavailableException;
import com.househost.auth.domain.exception.LoginTemporarilyBlockedException;
import com.househost.auth.domain.model.LoginSecurityScope;
import com.househost.auth.domain.model.User;
import com.househost.auth.domain.model.UserRole;
import com.househost.security.application.port.in.AccessControlUseCase;
import com.househost.security.application.port.in.TokenUseCase;
import com.househost.shared.exception.InvalidLoginException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthUseCase {
    // Porta usada para consultar e persistir usuarios sem acoplar a aplicacao ao JPA.
    private final UserPersistencePort persistencePort;
    // Porta que abstrai a codificacao e a comparacao segura de senhas.
    private final PasswordPort passwordPort;
    // Caso de uso responsavel por gerar o JWT e informar seu tempo de expiracao.
    private final TokenUseCase tokenUseCase;
    // Porta de saida usada para registrar eventos relevantes de autenticacao.
    private final AuthAuditPort auditPort;
    // Caso de uso que informa se o ator atual pode administrar usuarios.
    private final AccessControlUseCase accessControlUseCase;
    // Servico especializado nas validacoes de login e cadastro.
    private final AuthValidationService validationService;
    // Servico que consulta, incrementa e limpa os controles de tentativas de login.
    private final LoginSecurityService loginSecurityService;
    // Porta que envia alertas operacionais sobre comportamentos de alto risco.
    private final LoginSecurityAlertPort securityAlertPort;
    // Hash BCrypt ficticio usado para manter custo comparavel quando o email nao existe.
    private final String dummyPasswordHash;

    // O Spring injeta todas as dependencias necessarias para os casos de uso de autenticacao.
    public AuthService(UserPersistencePort persistencePort, PasswordPort passwordPort, TokenUseCase tokenUseCase,
            AuthAuditPort auditPort, AccessControlUseCase accessControlUseCase,
            AuthValidationService validationService, LoginSecurityService loginSecurityService,
            LoginSecurityAlertPort securityAlertPort,
            @Qualifier("loginDummyPasswordHash") String dummyPasswordHash) {
        // Guarda a porta de persistencia de usuarios.
        this.persistencePort = persistencePort;
        // Guarda a porta de operacoes criptograficas de senha.
        this.passwordPort = passwordPort;
        // Guarda o caso de uso de tokens.
        this.tokenUseCase = tokenUseCase;
        // Guarda a porta de auditoria.
        this.auditPort = auditPort;
        // Guarda o controle de acesso usado durante o cadastro.
        this.accessControlUseCase = accessControlUseCase;
        // Guarda o servico de validacao.
        this.validationService = validationService;
        // Guarda o servico da politica de falhas de login.
        this.loginSecurityService = loginSecurityService;
        // Guarda o canal abstrato de alertas operacionais.
        this.securityAlertPort = securityAlertPort;
        // Guarda o unico hash ficticio criado na inicializacao da aplicacao.
        this.dummyPasswordHash = dummyPasswordHash;
    }

    /**
     * Autentica um usuario administrativo aplicando a protecao contra repetidas
     * falhas antes de consultar senha ou gerar token.
     */
    public LoginResponseDTO login(LoginRequestDTO request, LoginRequestContextRecord loginRequestContextRecord) {
        // Rejeita requisicoes sem os campos minimos; tentativas malformadas nao entram nos contadores.
        validationService.validateLogin(request);
        // Normaliza o email para que maiusculas e espacos nao criem identidades de contagem diferentes.
        String normalizedEmail = request.email.trim().toLowerCase(Locale.ROOT);
        // Deriva uma referencia HMAC que pode ser auditada sem persistir um email desconhecido em claro.
        String emailHmacKey = deriveEmailHmacKey(normalizedEmail, loginRequestContextRecord);
        // Consulta os bloqueios ativos do par email/IP, do IP e da conta antes de qualquer BCrypt.
        Optional<ActiveLoginRestrictionRecord> activeLoginRestrictionRecordOptional = ensureAllowed(normalizedEmail, loginRequestContextRecord);
        // Entra neste bloco quando pelo menos um dos tres escopos ainda esta temporariamente bloqueado.
        if (activeLoginRestrictionRecordOptional.isPresent()) {
            // Extrai a restricao com maior tempo restante escolhida pelo LoginSecurityService.
            ActiveLoginRestrictionRecord activeLoginRestrictionRecord = activeLoginRestrictionRecordOptional.get();
            // Diferencia uma requisicao recusada por bloqueio ativo da tentativa que criou o bloqueio.
            auditPort.recordLoginOutcome("USER_LOGIN_RATE_LIMITED", null, emailHmacKey, loginRequestContextRecord,
                    Map.of("scope", activeLoginRestrictionRecord.scope().name(), "remainingSeconds", activeLoginRestrictionRecord.remainingSeconds()));
            // Interrompe o fluxo antes da consulta de usuario, comparacao de senha e geracao de JWT.
            throw new LoginTemporarilyBlockedException(activeLoginRestrictionRecord.remainingSeconds());
        }

        // Procura o usuario usando o email ja normalizado; Optional evita usar null como resultado normal.
        Optional<User> candidateOptional = persistencePort.findByEmail(normalizedEmail);
        // Usa o hash real quando a conta existe ou o hash ficticio quando ela nao existe.
        String comparisonHash = candidateOptional.map(User::getPasswordHash).orElse(dummyPasswordHash);
        // Executa exatamente uma comparacao de senha nos dois caminhos para reduzir diferencas de tempo.
        boolean credentialsMatch = passwordPort.matches(request.password, comparisonHash);
        // A autenticacao falha tanto para conta inexistente quanto para senha incorreta.
        if (candidateOptional.isEmpty() || !credentialsMatch) {
            // Incrementa atomicamente os escopos EMAIL_IP, IP e ACCOUNT e recebe suas transicoes.
            List<LoginSecurityFailureResultRecord> loginSecurityFailureResultRecordList = registerFailure(normalizedEmail, loginRequestContextRecord);
            // Usa o usuario interno somente se ele existir; email desconhecido nunca vira ator em claro.
            User knownUser = candidateOptional.orElse(null);
            // Registra a falha comparavel com a mesma categoria para email desconhecido e senha incorreta.
            auditPort.recordLoginOutcome("USER_LOGIN_FAILED", knownUser, emailHmacKey, loginRequestContextRecord,
                    Map.of("comparable", true));
            // Procura alguma atualizacao que tenha acabado de ativar um novo bloqueio.
            Optional<LoginSecurityFailureResultRecord> newlyBlockedLoginSecurityFailureResultRecordOptional = loginSecurityFailureResultRecordList.stream().filter(LoginSecurityFailureResultRecord::newlyBlocked)
                    // Se mais de um limite for atingido junto, preserva como referencia o bloqueio mais longo.
                    .max(java.util.Comparator.comparing(LoginSecurityFailureResultRecord::blockedUntil));
            // Processa separadamente a primeira transicao de liberado para bloqueado.
            if (newlyBlockedLoginSecurityFailureResultRecordOptional.isPresent()) {
                // Percorre os tres resultados porque mais de um escopo pode bloquear na mesma tentativa.
                for (LoginSecurityFailureResultRecord loginSecurityFailureResultRecord : loginSecurityFailureResultRecordList) {
                    // Ignora escopos apenas incrementados e trata somente os bloqueios recem-ativados.
                    if (loginSecurityFailureResultRecord.newlyBlocked()) {
                        // Registra qual escopo bloqueou e a contagem atingida, sem incluir credenciais.
                        auditPort.recordLoginOutcome("USER_LOGIN_BLOCKED", knownUser, emailHmacKey, loginRequestContextRecord,
                                Map.of("scope", loginSecurityFailureResultRecord.scope().name(), "count", loginSecurityFailureResultRecord.failureCount()));
                        // Escopos IP e ACCOUNT indicam padroes mais amplos e tambem exigem alerta operacional.
                        if (loginSecurityFailureResultRecord.scope() == LoginSecurityScope.IP
                            || loginSecurityFailureResultRecord.scope() == LoginSecurityScope.ACCOUNT) {
                            // Envia um alerta estruturado sem senha, token, payload ou email desconhecido em claro.
                            securityAlertPort.send(new LoginSecurityAlertMessageRecord(
                                    // IP representa pulverizacao; ACCOUNT representa ataque distribuido a uma conta.
                                    loginSecurityFailureResultRecord.scope() == LoginSecurityScope.IP ? "BROAD_IP_SPRAYING" : "DISTRIBUTED_ACCOUNT_TARGETING",
                                    loginSecurityFailureResultRecord.scope(), loginSecurityFailureResultRecord.failureCount(), loginSecurityFailureResultRecord.blockedUntil(), emailHmacKey,
                                    "Login failure threshold activated"));
                        }
                    }
                }
                // Rele o estado persistido para calcular com precisao o Retry-After devolvido ao cliente.
                ActiveLoginRestrictionRecord activeLoginRestrictionRecord = ensureAllowed(normalizedEmail, loginRequestContextRecord)
                        // Ausencia inesperada apos uma transicao de bloqueio e tratada como falha da protecao.
                        .orElseThrow(LoginProtectionUnavailableException::new);
                // A propria tentativa que atingiu o limite ja recebe HTTP 429.
                throw new LoginTemporarilyBlockedException(activeLoginRestrictionRecord.remainingSeconds());
            }
            // Falha abaixo dos limites mantem o contrato generico HTTP 401.
            throw new InvalidLoginException();
        }

        // Neste ponto o Optional necessariamente contem o usuario cuja senha foi validada.
        User user = candidateOptional.orElseThrow();
        // Limpa os estados do par e da conta; o estado geral do IP e intencionalmente preservado.
        registerSuccess(normalizedEmail, loginRequestContextRecord);
        // Gera o JWT somente depois de confirmar que nao ha bloqueio e que a limpeza foi persistida.
        String token = tokenUseCase.generateToken(user.getEmail());
        // Registra o sucesso associando o evento ao usuario interno autenticado.
        auditPort.recordForExplicitActor("USER_LOGIN_SUCCEEDED", user, Map.of("role", user.getRole().name()));
        // Mantem o formato de resposta de login que ja era consumido pelo frontend.
        return toResponse(user, token);
    }

    // Centraliza a derivacao da chave protegida e converte qualquer falha em indisponibilidade segura.
    private String deriveEmailHmacKey(String email, LoginRequestContextRecord loginRequestContextRecord) {
        try {
            // Delega ao servico, que normaliza e aplica o adapter HMAC dedicado.
            return loginSecurityService.deriveEmailHmacKey(email);
        } catch (RuntimeException exception) {
            // Produz evidencia operacional antes de recusar a autenticacao.
            protectionUnavailable(loginRequestContextRecord, exception);
            // Falha fechada: o problema na protecao nunca libera o login.
            throw new LoginProtectionUnavailableException(exception);
        }
    }

    // Consulta restricoes mantendo o tratamento fail-closed fora do fluxo principal.
    private Optional<ActiveLoginRestrictionRecord> ensureAllowed(String email, LoginRequestContextRecord loginRequestContextRecord) {
        try {
            // Usa somente o IP previamente resolvido pelo adapter de origem confiavel.
            return loginSecurityService.ensureAllowed(email, loginRequestContextRecord.ipAddress());
        } catch (RuntimeException exception) {
            // Alerta e audita a indisponibilidade do estado de protecao.
            protectionUnavailable(loginRequestContextRecord, exception);
            // Converte falhas de infraestrutura em uma excecao generica de servico indisponivel.
            throw new LoginProtectionUnavailableException(exception);
        }
    }

    // Registra uma falha nos tres escopos sem permitir bypass em caso de erro de persistencia.
    private List<LoginSecurityFailureResultRecord> registerFailure(String email, LoginRequestContextRecord loginRequestContextRecord) {
        try {
            // O servico devolve contagens e informa quais escopos bloquearam nesta operacao.
            return loginSecurityService.registerFailure(email, loginRequestContextRecord.ipAddress());
        } catch (RuntimeException exception) {
            // Sinaliza que o mecanismo obrigatorio de protecao nao conseguiu atualizar seu estado.
            protectionUnavailable(loginRequestContextRecord, exception);
            // Recusa o login em vez de continuar sem registrar a tentativa.
            throw new LoginProtectionUnavailableException(exception);
        }
    }

    // Limpa somente os estados permitidos pela especificacao depois de uma senha valida.
    private void registerSuccess(String email, LoginRequestContextRecord loginRequestContextRecord) {
        try {
            // LoginSecurityService limpa EMAIL_IP e ACCOUNT, mas conserva o historico recente do IP.
            loginSecurityService.registerSuccess(email, loginRequestContextRecord.ipAddress());
        } catch (RuntimeException exception) {
            // Uma falha de limpeza tambem significa que o estado de protecao deixou de ser confiavel.
            protectionUnavailable(loginRequestContextRecord, exception);
            // O JWT nao sera gerado quando a atualizacao do controle de sucesso falhar.
            throw new LoginProtectionUnavailableException(exception);
        }
    }

    // Gera sinais independentes de alerta e auditoria quando a protecao de login falha.
    private void protectionUnavailable(LoginRequestContextRecord loginRequestContextRecord, RuntimeException exception) {
        try {
            // Tenta avisar o canal operacional com apenas o tipo tecnico da falha.
            securityAlertPort.send(new LoginSecurityAlertMessageRecord("LOGIN_PROTECTION_UNAVAILABLE", null, 0,
                    Instant.now(), null, exception.getClass().getSimpleName()));
        } catch (RuntimeException ignored) {
            // Mesmo que o alerta falhe, a autenticacao continuara sendo recusada de forma segura.
        }
        // Registra a indisponibilidade na auditoria sem expor a mensagem interna da excecao.
        auditPort.recordLoginOutcome("LOGIN_PROTECTION_UNAVAILABLE", null, null, loginRequestContextRecord,
                Map.of("failureType", exception.getClass().getSimpleName()));
    }

    /** Cadastra um usuario respeitando validacao, permissao de papel, hash de senha e auditoria. */
    public RegistrationResponseDTO registration(RegistrationRequestDTO request) {
        // Verifica campos obrigatorios e duplicidade de username e email.
        validationService.validateRegistration(request);
        // Somente quem pode administrar usuarios escolhe o papel; os demais criam RECEPTION.
        UserRole role = accessControlUseCase.canManageUsers() && request.role != null
                ? request.role
                : UserRole.RECEPTION;

        // Constroi o modelo de dominio e o persiste atraves da porta de saida.
        User user = persistencePort.save(
                new User(request.username.trim(),
                        request.email.trim(),
                        // Nunca persiste a senha recebida; persiste somente seu hash seguro.
                        passwordPort.encode(request.password),
                        role,
                        // Converte foto vazia em null e remove espacos quando houver valor.
                        blankToNull(request.photoUrl)));

        // Registra quem foi criado e qual papel recebeu.
        auditPort.recordForExplicitActor("USER_CREATED", user, Map.of("role", role.name()));
        // Devolve somente os dados publicos definidos pelo contrato de cadastro.
        return new RegistrationResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.getPhone(), role, user.getPhotoUrl());
    }

    // Traduz o modelo autenticado e o token para o DTO devolvido pela API.
    private LoginResponseDTO toResponse(User user, String token) {
        return new LoginResponseDTO(user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getPhotoUrl(),
                token,
                // Informa expiracao somente quando efetivamente existe um token.
                token == null ? null : tokenUseCase.getExpirationSeconds());
    }

    // Normaliza campos opcionais para evitar armazenar string vazia como se fosse informacao real.
    private String blankToNull(String value) {
        // Retorna null para ausente/vazio; caso contrario, devolve o texto sem espacos externos.
        return validationService.isBlank(value) ? null : value.trim();
    }
}
