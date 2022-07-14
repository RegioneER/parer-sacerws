# SACER WS

Progetto contenente tutti i web services esposti da sacer.

    Fornitore: Engineering
    Struttura_RER: PARER

## Configurazioni per JBoss 6 EAP

Vedere i seguenti documenti:
- [Configurazioni generali](src/docs/JBoss6_configurazione_generale.md)
- [Configurazioni specifiche di progetto](src/docs/JBoss6_configurazione_sacerws.md)

#### TODO 

- Chiamata API per la gestione (quando si crea il tag git) di una relativa "documentazione" al fine di avere un report di tutte le relase (vedi menu laterale)

```
curl --request POST --header 'Content-Type: application/json' --header "Private-Token: <TOKEN>" --data '{"name": "sacerws-2.1.1", "tag_name": "sacerws-2.1.1", "description": "Test"}' "https://gitlab.ente.regione.emr.it/api/v4/projects/733/releases"
```
Nota: 733 è l'ID del progetto / probabilmente su pipeline esiste una funzionalità apposita oppure si richiama l'API
