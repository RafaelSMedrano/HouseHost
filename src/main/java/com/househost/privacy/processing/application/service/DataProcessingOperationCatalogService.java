package com.househost.privacy.processing.application.service;

import com.househost.privacy.processing.application.port.in.DataProcessingOperationCatalogUseCase;
import com.househost.privacy.processing.application.port.out.DataProcessingOperationPersistencePort;
import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import com.househost.privacy.processing.domain.model.DataProcessingOperationNames;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataProcessingOperationCatalogService implements DataProcessingOperationCatalogUseCase {

    private final DataProcessingOperationPersistencePort persistencePort;

    public DataProcessingOperationCatalogService(
            DataProcessingOperationPersistencePort persistencePort
    ) {
        this.persistencePort = persistencePort;
    }

    @Override
    @Transactional
    public void initializeCatalog() {
        initialOperations().stream()
                .filter(operation -> !persistencePort.existsByOperationName(
                        operation.getOperationName()
                ))
                .forEach(persistencePort::save);
        deactivateExistingWhatsappMarketing();
    }

    public List<DataProcessingOperation> initialOperations() {
        return List.of(
                bookingManagement(),
                guestManagement(),
                stayManagement(),
                guestFinancialManagement(),
                whatsappMarketing(),
                userAccessManagement(),
                supplierGovernance(),
                securityAuditManagement(),
                privacyGovernance()
        );
    }

    private DataProcessingOperation bookingManagement() {
        return operation(
                DataProcessingOperationCodes.BOOKING_MANAGEMENT,
                DataProcessingOperationNames.BOOKING_MANAGEMENT,
                "Recebimento, criacao, confirmacao, alteracao e cancelamento de reservas e pre-reservas.",
                "Verificar disponibilidade, administrar a reserva e manter contato operacional com o hospede.",
                "EXECUCAO_DE_CONTRATO_OU_PROCEDIMENTOS_PRELIMINARES",
                "Hospedes e responsaveis pela reserva",
                "Nome, telefone, e-mail, cidade, datas da estadia, quantidade de hospedes, pets, pedidos especiais e registros de aceite",
                "Site publico, WhatsApp, telefone e painel administrativo",
                "Coleta, registro, armazenamento, consulta, atualizacao, confirmacao, cancelamento e exclusao ou anonimizacao",
                "Administracao, gerencia e recepcao",
                "Provedores de infraestrutura, banco de dados e comunicacao autorizados",
                false,
                "Conforme a politica de retencao; pre-reservas nao confirmadas devem ter prazo reduzido",
                "Exclusao ou anonimizacao ao termino do prazo aplicavel",
                "Autenticacao, controle de acesso, validacao, auditoria, HTTPS e backups protegidos",
                "Administracao e reservas"
        );
    }

    private DataProcessingOperation guestManagement() {
        return operation(
                DataProcessingOperationCodes.GUEST_MANAGEMENT,
                DataProcessingOperationNames.GUEST_MANAGEMENT,
                "Manutencao do cadastro e historico operacional dos hospedes.",
                "Identificar hospedes, facilitar atendimentos e manter informacoes necessarias para reservas e hospedagens.",
                "EXECUCAO_DE_CONTRATO_OU_PROCEDIMENTOS_PRELIMINARES",
                "Hospedes",
                "Nome, e-mail, telefone, documento quando aplicavel, endereco, nascimento, preferencias, acessibilidade, historico e observacoes",
                "Hospede, responsavel pela reserva e equipe administrativa",
                "Coleta, registro, consulta, atualizacao, relacionamento com reservas e hospedagens, exclusao ou anonimizacao",
                "Administracao, gerencia e recepcao",
                "Provedores de infraestrutura e banco de dados autorizados",
                false,
                "Conforme necessidade operacional e obrigacoes legais aplicaveis",
                "Exclusao, anonimizacao ou restricao de acesso conforme o caso",
                "Autenticacao, controle de acesso, mascaramento, auditoria e backups protegidos",
                "Administracao e atendimento"
        );
    }

    private DataProcessingOperation stayManagement() {
        return operation(
                DataProcessingOperationCodes.STAY_MANAGEMENT,
                DataProcessingOperationNames.STAY_MANAGEMENT,
                "Registro e acompanhamento da entrada, permanencia e saida do hospede.",
                "Executar a hospedagem, controlar ocupacao, entregar chaves e registrar procedimentos operacionais.",
                "EXECUCAO_DE_CONTRATO_E_CUMPRIMENTO_DE_OBRIGACAO_LEGAL",
                "Hospedes e acompanhantes",
                "Identificacao do hospede, datas e horarios, quarto, quantidade de acompanhantes, verificacao documental, veiculo, ocorrencias e observacoes",
                "Reserva, hospede e equipe de atendimento",
                "Registro, consulta, atualizacao, verificacao, armazenamento e encerramento da hospedagem",
                "Administracao, gerencia, recepcao e equipe operacional conforme necessidade",
                "Autoridades publicas quando houver obrigacao legal e provedores de infraestrutura autorizados",
                false,
                "Conforme obrigacoes legais e politica de retencao da hospedagem",
                "Exclusao ou anonimizacao apos o prazo legal e operacional aplicavel",
                "Autenticacao, controle de acesso por perfil, auditoria, HTTPS e backups protegidos",
                "Operacao e recepcao"
        );
    }

    private DataProcessingOperation guestFinancialManagement() {
        return operation(
                DataProcessingOperationCodes.FINANCIAL_MANAGEMENT,
                DataProcessingOperationNames.FINANCIAL_MANAGEMENT,
                "Registro de valores, pagamentos, pendencias e transacoes vinculadas a reservas ou hospedes.",
                "Controlar cobrancas, pagamentos e situacao financeira da hospedagem.",
                "EXECUCAO_DE_CONTRATO_E_CUMPRIMENTO_DE_OBRIGACAO_LEGAL",
                "Hospedes e responsaveis financeiros",
                "Identificador do hospede, reserva, valores, datas, forma e status de pagamento, descontos, parcelas e descricao da transacao",
                "Reservas, hospedes, equipe administrativa e registros de pagamento",
                "Registro, consulta, conciliacao, atualizacao, liquidacao e armazenamento",
                "Administracao, gerencia e perfis financeiros autorizados",
                "Instituicoes financeiras, meios de pagamento, contabilidade e provedores autorizados quando aplicavel",
                false,
                "Conforme prazos fiscais, contabeis e contratuais aplicaveis",
                "Exclusao ou anonimizacao depois do cumprimento dos prazos legais",
                "Controle de acesso restrito, auditoria, backups protegidos e proibicao de armazenar dados completos de cartao",
                "Administracao e financeiro"
        );
    }

    private DataProcessingOperation whatsappMarketing() {
        DataProcessingOperation marketingOperation = operation(
                DataProcessingOperationCodes.WHATSAPP_MARKETING,
                DataProcessingOperationNames.WHATSAPP_MARKETING,
                "Envio de promocoes e comunicacoes comerciais para pessoas que aceitaram receber marketing.",
                "Divulgar ofertas e manter relacionamento comercial com consentimento separado da reserva.",
                "CONSENTIMENTO",
                "Hospedes e interessados que realizaram opt-in",
                "Nome, telefone, registro do consentimento, data do consentimento e preferencias de comunicacao",
                "Formulario publico, atendimento e manifestacao direta do titular",
                "Coleta do consentimento, armazenamento, segmentacao, envio de mensagens, revogacao e exclusao",
                "Administracao e marketing autorizado",
                "WhatsApp e provedores de comunicacao autorizados",
                true,
                "Ate a revogacao do consentimento ou encerramento da finalidade",
                "Remocao da lista e conservacao apenas do registro minimo necessario para comprovar a revogacao",
                "Opt-in separado, controle de acesso, registro de consentimento e mecanismo de descadastramento",
                "Administracao e marketing"
        );
        marketingOperation.changeStatus(DataProcessingOperationStatus.INACTIVE);
        return marketingOperation;
    }

    private DataProcessingOperation userAccessManagement() {
        return operation(
                DataProcessingOperationCodes.USER_ACCESS_MANAGEMENT,
                DataProcessingOperationNames.USER_ACCESS_MANAGEMENT,
                "Cadastro, autenticacao e administracao de usuarios internos do HouseHost.",
                "Permitir acesso seguro ao sistema, aplicar perfis e responsabilizar operacoes administrativas.",
                "LEGITIMO_INTERESSE_E_EXECUCAO_DE_RELACAO_CONTRATUAL",
                "Administradores, gestores, recepcionistas e demais usuarios internos",
                "Nome de usuario, e-mail, telefone, perfil de acesso, foto, credencial protegida e registros de autenticacao",
                "Proprio usuario e administradores autorizados",
                "Cadastro, autenticacao, autorizacao, atualizacao, bloqueio e armazenamento",
                "Administradores autorizados e suporte tecnico quando necessario",
                "Provedores de infraestrutura e autenticacao autorizados",
                false,
                "Durante o vinculo e pelo prazo necessario para seguranca e responsabilizacao",
                "Bloqueio do acesso e exclusao ou anonimizacao apos os prazos aplicaveis",
                "Hash de senha, JWT, controle de acesso, minimo privilegio, auditoria e HTTPS",
                "Administracao e tecnologia"
        );
    }

    private DataProcessingOperation supplierGovernance() {
        return operation(
                DataProcessingOperationCodes.SUPPLIER_GOVERNANCE,
                DataProcessingOperationNames.SUPPLIER_GOVERNANCE,
                "Cadastro, avaliacao e revisao de fornecedores e suas relacoes de tratamento de dados.",
                "Manter evidencia de governanca, contratos, seguranca, retencao e responsabilidades de terceiros.",
                "CUMPRIMENTO_DE_OBRIGACAO_LEGAL_E_LEGITIMO_INTERESSE",
                "Fornecedores, contatos funcionais e titulares cujos dados sao tratados pelos servicos",
                "Identificacao empresarial, contatos funcionais, categorias de dados e evidencia de governanca",
                "Equipe administrativa, contratos e informacoes fornecidas pelos prestadores",
                "Cadastro, consulta, atualizacao, avaliacao, revisao, bloqueio e inativacao",
                "CEO, CTO e administradores autorizados",
                "Provedores registrados no inventario conforme sua relacao real",
                true,
                "Durante a relacao e pelos prazos legais, contratuais e de responsabilizacao aplicaveis",
                "Inativacao, devolucao, eliminacao ou retencao justificada",
                "Controle de acesso, auditoria, minimizacao e revisao periodica",
                "Administracao e privacidade"
        );
    }

    private DataProcessingOperation securityAuditManagement() {
        return operation(
                DataProcessingOperationCodes.SECURITY_AUDIT_MANAGEMENT,
                DataProcessingOperationNames.SECURITY_AUDIT_MANAGEMENT,
                "Registro e analise de autenticacao, autorizacao, acessos, alertas e acontecimentos relevantes de seguranca.",
                "Prevenir abuso, detectar acessos indevidos, manter rastreabilidade, investigar incidentes e proteger direitos.",
                "LEGITIMO_INTERESSE",
                "Usuarios internos, hospedes, solicitantes de reserva e pessoas relacionadas a eventos de seguranca",
                "Identificadores de ator e entidade, IP, user-agent, data, horario, tipo, resultado, identificador derivado por HMAC e metadados minimos",
                "Login, requisicoes HTTP, API publica, painel administrativo, modulos de negocio e infraestrutura",
                "Geracao, coleta, armazenamento, consulta, correlacao, investigacao, exportacao restrita e eliminacao",
                "CEO, CTO, administradores e responsavel tecnico autorizado",
                "Provedores de infraestrutura ou monitoramento registrados e autoridades quando legalmente necessario",
                false,
                "Estado de protecao de login por 30 dias e eventos de auditoria por 12 meses, salvo preservacao justificada para incidente, obrigacao ou exercicio de direitos",
                "Expiracao e eliminacao controlada ao final do prazo, ou anonimizacao quando a finalidade admitir",
                "Controle de acesso restrito, minimizacao, HMAC, protecao de logs, integridade, alertas e auditoria de consulta",
                "Tecnologia, seguranca e privacidade"
        );
    }

    private DataProcessingOperation privacyGovernance() {
        return operation(
                DataProcessingOperationCodes.PRIVACY_GOVERNANCE,
                DataProcessingOperationNames.PRIVACY_GOVERNANCE,
                "Registro, avaliacao, revisao e aprovacao das bases legais das operacoes de tratamento.",
                "Manter evidencias de conformidade, responsabilizacao e governanca previstas na LGPD.",
                "CUMPRIMENTO_DE_OBRIGACAO_LEGAL_E_LEGITIMO_INTERESSE",
                "Titulares relacionados as operacoes inventariadas e revisores internos",
                "Identificadores das operacoes, bases legais, justificativas, evidencias e identificador do revisor",
                "Inventario de tratamentos, areas responsaveis e revisores autorizados",
                "Registro, consulta, revisao, aprovacao, versionamento e auditoria",
                "CEO, CTO e administradores autorizados",
                "Assessoria juridica ou autoridade competente quando legitimamente necessario",
                false,
                "Durante a vigencia da operacao e pelos prazos de responsabilizacao aplicaveis",
                "Eliminacao controlada ao termino da finalidade e dos prazos aplicaveis",
                "Controle de acesso administrativo, versionamento, imutabilidade de aprovados e auditoria minimizada",
                "Administracao e privacidade"
        );
    }

    private void deactivateExistingWhatsappMarketing() {
        persistencePort.findByOperationCode(DataProcessingOperationCodes.WHATSAPP_MARKETING)
                .filter(operation -> operation.getStatus() != DataProcessingOperationStatus.INACTIVE)
                .ifPresent(operation -> {
                    operation.changeStatus(DataProcessingOperationStatus.INACTIVE);
                    persistencePort.save(operation);
                });
    }

    private DataProcessingOperation operation(
            String operationCode,
            String operationName,
            String description,
            String purpose,
            String legalBasis,
            String dataSubjectCategories,
            String personalDataCategories,
            String dataSource,
            String processingActions,
            String internalAccessRoles,
            String externalRecipients,
            boolean internationalTransfer,
            String retentionPeriod,
            String deletionMethod,
            String securityMeasures,
            String responsibleArea
    ) {
        return new DataProcessingOperation(
                operationCode,
                operationName,
                description,
                purpose,
                legalBasis,
                dataSubjectCategories,
                personalDataCategories,
                dataSource,
                processingActions,
                internalAccessRoles,
                externalRecipients,
                internationalTransfer,
                retentionPeriod,
                deletionMethod,
                securityMeasures,
                responsibleArea,
                "HouseHost"
        );
    }
}
