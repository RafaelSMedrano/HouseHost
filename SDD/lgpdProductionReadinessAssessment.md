# Avaliação de Prontidão para Produção — LGPD

Data da avaliação: 27 de julho de 2026.

Requisito de referência: `SDD/specs/lgpdGovernanceSpec.md`, seção
`Production Readiness Requirements`.

Esta é uma avaliação técnica e documental do projeto. Ela não constitui
parecer jurídico nem certificação de conformidade com a LGPD.

| Item | Situação | Avaliação |
| --- | --- | --- |
| 1. Controlador e operadores | Parcial | A política identifica o controlador e categorias genéricas de operadores, mas não existe inventário nominal de operadores, suboperadores, contratos e responsabilidades. Evidência: `frontend/public/js/views/politicaPrivacidadeView.js:28`. |
| 2. Inventário de tratamentos | Parcial | Existe um módulo estruturado com finalidade, base legal, titulares, destinatários, retenção e descarte: `src/main/java/com/househost/privacy/domain/model/DataProcessingOperation.java:8`. Entretanto, o catálogo inicial não cobre claramente auditoria e segurança e ainda contém marketing como operação ativa: `src/main/java/com/househost/privacy/application/service/DataProcessingOperationCatalogService.java:34`. |
| 3. Minimização | Parcial | O fluxo público reduz a coleta e a política orienta contra documentos, dados financeiros e sensíveis: `frontend/public/js/views/politicaPrivacidadeView.js:37`. Ainda faltam limites e validações completas, conforme a própria análise do projeto: `README.md:9672`. |
| 4. Bases legais | Parcial | O catálogo registra bases legais, mas elas ainda são declarações gerais e precisam ser confirmadas para os tratamentos reais: `src/main/java/com/househost/privacy/application/service/DataProcessingOperationCatalogService.java:45`. |
| 5. Política pública | Quase atendido | Existe política clara com controlador, finalidade, compartilhamento, retenção, direitos e canal: `frontend/public/js/views/politicaPrivacidadeView.js:19`. A versão, porém, está fixa no frontend e não é governada pelo servidor. |
| 6. Solicitações de titulares | Não atendido | Existe o canal por WhatsApp, mas não há workflow para autenticar, registrar, acompanhar, responder e executar solicitações. A lacuna está reconhecida em `README.md:9534`. |
| 7. Encarregado ou dispensa | Não comprovado | Não foi encontrada indicação formal de encarregado nem documento justificando eventual dispensa aplicável a agente de pequeno porte. |
| 8. Retenção e descarte | Não atendido | O catálogo contém descrições genéricas, mas não há matriz com prazos concretos nem execução abrangente de eliminação e anonimização: `README.md:9485`. |
| 9. Privacy by design | Parcial | Há minimização, fachada pública separada, mascaramento e auditoria. Porém não existe uma etapa formal obrigatória de avaliação de privacidade para cada mudança. |
| 10. Segurança e mínimo privilégio | Parcial | Há JWT, autorização por função e proteção de rotas: `src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java:55`. Entretanto, CORS aceita qualquer origem: `src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java:104`. Também faltam evidências de TLS obrigatório, criptografia em repouso, backup seguro, restauração testada, correção de vulnerabilidades e rotação de segredos. |
| 11. Separação entre produção e testes | Não comprovado | A spec proíbe o uso indevido, mas não foi encontrado procedimento, controle ou evidência de que dados reais nunca sejam copiados para ambientes não produtivos. |
| 12. Evidências auditáveis | Parcial | Existem auditoria e revisão do catálogo: `src/main/java/com/househost/privacy/adapter/in/rest/DataProcessingOperationController.java:66`. Não existem registros estruturados para solicitações de titulares, eliminações, compartilhamentos e revisões completas de conformidade. |
| 13. Fornecedores | Não atendido | Não foi encontrada avaliação documental de fornecedores, contratos, retenção, suboperadores e comunicação de incidentes. |
| 14. Transferências internacionais | Não atendido | O catálogo possui um indicador, mas isso não comprova o levantamento real dos fornecedores nem a existência de mecanismo autorizado de transferência. |
| 15. Dados sensíveis e crianças | Parcial | O fluxo público orienta contra dados sensíveis: `frontend/public/js/views/politicaPrivacidadeView.js:45`. Porém o sistema administrativo admite informações como acessibilidade, nascimento e acompanhantes sem evidência de classificação e salvaguardas específicas completas. |
| 16. Avaliação de risco e RIPD | Não atendido | Não foi encontrada avaliação formal de risco de privacidade nem RIPD, inclusive decisão documentada sobre sua necessidade ou não. |
| 17. Plano de incidentes | Não atendido | A documentação descreve o que deveria existir, mas reconhece que o processo precisa ser criado: `README.md:9630`. Não há evidência de simulação ou teste. |
| 18. Comunicação à ANPD e titulares | Não atendido | Não foi encontrado workflow, responsáveis, modelos de comunicação, registro de decisão ou controle do prazo regulatório. |
| 19. Treinamento e revisões periódicas | Não comprovado | Existe revisão individual do catálogo, mas não há evidência de treinamento da equipe nem calendário de revisão de permissões, fornecedores, retenção e solicitações. |
| 20. Revisão final de prontidão | Não atendido | Não existe relatório formal confirmando requisito por requisito, evidência, teste, responsável, risco residual e aprovação para produção. |

## Avaliação geral

O projeto ainda não deve ser considerado pronto para uso em produção com dados
pessoais reais de acordo com sua própria especificação de governança LGPD. Há
uma fundação técnica parcial, mas controles organizacionais, procedimentos
executáveis e evidências documentadas permanecem incompletos.

Os principais bloqueadores são:

1. fluxo completo para direitos dos titulares;
2. matriz e execução de retenção, descarte e anonimização;
3. inventário e contratos de operadores e transferências;
4. plano de resposta a incidentes testado;
5. definição de encarregado ou justificativa formal de dispensa;
6. avaliação de risco e decisão sobre RIPD;
7. controles comprovados de infraestrutura para TLS, backups, criptografia,
   segredos, monitoramento e vulnerabilidades;
8. treinamento e responsabilidades organizacionais;
9. relatório formal de prontidão com responsáveis e evidências.
