
## 6.6.0 (31-03-2025)

### Bugfix: 1
- [#37578](https://parermine.regione.emilia-romagna.it/issues/37578) Correzione per la mancata chiusura dei file e protocollo NFS

## 6.5.0 (24-03-2025)

### Novità: 2
- [#37560](https://parermine.regione.emilia-romagna.it/issues/37560) Modifica gestione controllo delle revoche su marche temporali 
- [#37529](https://parermine.regione.emilia-romagna.it/issues/37529) Adeguamento salvataggio timestamp log stato conservazione UD

## 6.4.0 (13-02-2025)

### Bugfix: 1
- [#35911](https://parermine.regione.emilia-romagna.it/issues/35911) Correzione gestione del messaggio di controllo sui formati per documenti firmati con più livelli di buste individuate in fase di verifica firma

### Novità: 2
- [#35553](https://parermine.regione.emilia-romagna.it/issues/35553) Configurazione modulo jaxp-jdk per JBoss 7 per JDK 11
- [#34005](https://parermine.regione.emilia-romagna.it/issues/34005) Migrazione alle nuove dipendenze / pattern legate a xecers, xalan, jaxb, ecc

## 6.3.0 (29-01-2025)

### Novità: 1
- [#34873](https://parermine.regione.emilia-romagna.it/issues/34873) Aggiornamento modelli DSS 6.1

## 6.2.0 (20-01-2025)

### Novità: 1
- [#34697](https://parermine.regione.emilia-romagna.it/issues/34697) Aggiornamento libreria DSS 6.0

## 6.1.0 (09-12-2024)

### Bugfix: 1
- [#33853](https://parermine.regione.emilia-romagna.it/issues/33853) WS aggiornamento metadati UD: correzione del messaggio di controllo del profilo normativo nella response

### Novità: 4
- [#34196](https://parermine.regione.emilia-romagna.it/issues/34196) Nuova gestione per rapprentazione completa su base dati del seriale del certificato (CA, CRL e OCSP)
- [#34076](https://parermine.regione.emilia-romagna.it/issues/34076) Log del processo di conservazione delle unità documentarie
- [#33489](https://parermine.regione.emilia-romagna.it/issues/33489) Aggiornamento librerie obsolete 2024
- [#33128](https://parermine.regione.emilia-romagna.it/issues/33128) Aggiornamento alle ultimi versioni librerie jakarata-ee8 per jboss 7.4

## 6.0.0 (13-08-2024)

### Novità: 1
- [#30804](https://parermine.regione.emilia-romagna.it/issues/30804) Aggiornamento a Java 11

## 5.4.0 (18-07-2024)

### Bugfix: 1
- [#32809](https://parermine.regione.emilia-romagna.it/issues/32809) Correzione messaggio di errore creazione / copia oggetto "componente" (AWS S3) 

### Novità: 1
- [#32817](https://parermine.regione.emilia-romagna.it/issues/32817) Modifica modello CRYPTO response

## 5.3.0 (07-06-2024)

### Bugfix: 1
- [#32033](https://parermine.regione.emilia-romagna.it/issues/32033) Aggiornamento metadati: gestire errore dovuto all'aggiornamento errato del TipoConservazione

### Novità: 1
- [#32653](https://parermine.regione.emilia-romagna.it/issues/32653) Gestione invocazione microservice di verifica firma

## 5.2.0 (06-05-2024)

### Bugfix: 3
- [#32027](https://parermine.regione.emilia-romagna.it/issues/32027) Aggiornamento metadati: errore 666 se si tenta di aggiornare i Dati Specifici dei componenti
- [#31821](https://parermine.regione.emilia-romagna.it/issues/31821) Correzione gestione data scadenza in caso di "certificato scaduto" (verifica firma eidas)
- [#31285](https://parermine.regione.emilia-romagna.it/issues/31285) Correzione messaggio di errore di aggiornamento metadati

### Novità: 2
- [#31672](https://parermine.regione.emilia-romagna.it/issues/31672) Introduzione TAG "temporany" su bucket staging (con lifecycle)
- [#29276](https://parermine.regione.emilia-romagna.it/issues/29276) Salvataggio diretto su OS di Aggiornamento metadati UD

## 5.1.0 (04-04-2024)

### Bugfix: 3
- [#31300](https://parermine.regione.emilia-romagna.it/issues/31300) Aggiornamento metadati: errore 666 se si tenta di aggiornare i componenti
- [#31297](https://parermine.regione.emilia-romagna.it/issues/31297) Risoluzione errore 666 per AggiuntaAllegatiSync in caso di mancanza tag opzionale Configurazione
- [#29906](https://parermine.regione.emilia-romagna.it/issues/29906) Correzione della mancata gestione dell'errore per dati specifici che non coincidono con l'XSD

## 5.0.0 (12-02-2024)

### Novità: 1
- [#30800](https://parermine.regione.emilia-romagna.it/issues/30800) Aggiornamento a Spring 5 

## 4.14.0 (29-01-2024)

### Novità: 1
- [#31011](https://parermine.regione.emilia-romagna.it/issues/31011) Aggiornamento libreria DSS 5.13

## 4.13.0 (05-12-2023)

### Bugfix: 1
- [#30753](https://parermine.regione.emilia-romagna.it/issues/30753) Correzione su aggiornamento informazioni certification authority

### Novità: 2
- [#30849](https://parermine.regione.emilia-romagna.it/issues/30849) Creazione endpoint con informazioni sulla versione
- [#30718](https://parermine.regione.emilia-romagna.it/issues/30718) Eliminazione gestione persistenza di oggetti "raw" (base64) presenti su report EIDAS/CRYPTO

## 4.12.0 (22-11-2023)

### Bugfix: 1
- [#30887](https://parermine.regione.emilia-romagna.it/issues/30887) Correzione bug mancato salvataggio sessioni di versamento aggiunta documenti 

### Novità: 4
- [#30155](https://parermine.regione.emilia-romagna.it/issues/30155) Miglioramento della rappresentazione degli esiti dei controlli di revoca del certificato
- [#30037](https://parermine.regione.emilia-romagna.it/issues/30037) Gestione dell'errore: Rapporto versamento non disponibile
- [#29958](https://parermine.regione.emilia-romagna.it/issues/29958) Utilizzo degli EJB nella parte di aggiornamento metadati
- [#29613](https://parermine.regione.emilia-romagna.it/issues/29613) Rimozione della libreria esterna sacer-jpa

## 4.11.1 (05-10-2023)

### Bugfix: 1
- [#30617](https://parermine.regione.emilia-romagna.it/issues/30617) Correzione configurazione per ripristino della versione 1.11.1 di Eidas

## 4.11.0 (03-10-2023)

### Novità: 4
- [#30236](https://parermine.regione.emilia-romagna.it/issues/30236) Ripristino della versione 1.11.1 di Eidas
- [#30089](https://parermine.regione.emilia-romagna.it/issues/30089) Revisione partizionamenti aggiornamento metadati: aggiunta gestione campi per partizionamento
- [#29834](https://parermine.regione.emilia-romagna.it/issues/29834) Attività per la realizzazione della separazione delle sessioni: parte scrittura 
- [#28309](https://parermine.regione.emilia-romagna.it/issues/28309) Servizio aggiornamento metadati: possibilità di eliminare l'elemento DatiSpecifici

## 4.10.1 (13-09-2023)

### Bugfix: 1
- [#30211](https://parermine.regione.emilia-romagna.it/issues/30211) Downgrade alla versione 1.10.0 di Eidas

## 4.10.0 (16-08-2023)

### Bugfix: 1
- [#30052](https://parermine.regione.emilia-romagna.it/issues/30052) Correzione su regressione parsing del report di verifica firma EIDAS-DSS 5.12

### Novità: 2
- [#29881](https://parermine.regione.emilia-romagna.it/issues/29881) Aggiornamento alle versione 5.12 della libreria DSS
- [#29827](https://parermine.regione.emilia-romagna.it/issues/29827) Introduzione di tag sui versamenti non andati a buon fine per la definizione di politiche di retention

## 4.9.0 (03-08-2023)

### Novità: 1
- [#29661](https://parermine.regione.emilia-romagna.it/issues/29661) Aggiornamento librerie obsolete 2023

## 4.8.0 (13-07-2023)

### Bugfix: 1
- [#29615](https://parermine.regione.emilia-romagna.it/issues/29615) Correzione errata valorizzazione del TipoRiferimentoTemporaleUsato in caso di verifica Crypto effettuata alla data di firma se il documento è privo di data di firma

### Novità: 1
- [#26423](https://parermine.regione.emilia-romagna.it/issues/26423) Servizio di Aggiornamento UD: aggiungere profilo normativo

## 4.7.1 (15-06-2023)

### Bugfix: 3
- [#29761](https://parermine.regione.emilia-romagna.it/issues/29761) Correzione salvataggio evidenze di conservazione su versamento fascicolo 1.1 e 1.0
- [#29645](https://parermine.regione.emilia-romagna.it/issues/29645) Correzione di un bug in fase di versamento
- [#29457](https://parermine.regione.emilia-romagna.it/issues/29457) Correzione errore nella verifica delle autorizzazioni

### Novità: 1
- [#29091](https://parermine.regione.emilia-romagna.it/issues/29091) Verifica di conformità sull'attributo "Signature acceptance validation"

## 4.7.0 (19-05-2023)

### Novità: 3
- [#29084](https://parermine.regione.emilia-romagna.it/issues/29084) Separazione su Object Storage le evidenze di conservazione andate a buon fine dalle altre
- [#28970](https://parermine.regione.emilia-romagna.it/issues/28970) Gestione risposta Verifica Eidas se impostato a true UtilizzoDataFirmaPerRifTemp
- [#28489](https://parermine.regione.emilia-romagna.it/issues/28489) Eliminazione del salvataggio dei certificati della CA e delle CRL in fase di verifica

## 4.6.0 (08-05-2023)

### Novità: 1
- [#28542](https://parermine.regione.emilia-romagna.it/issues/28542) Creazione pacchetto unico per sacerws

## 4.5.1.1 (04-05-2023)

### Bugfix: 1
- [#29500](https://parermine.regione.emilia-romagna.it/issues/29500) Correzione gestione upload multipart large data object con RestTemplate (invocazione microservizio di verifica firma) 
## 4.5.1 (05-04-2023)

### Bugfix: 6
- [#28747](https://parermine.regione.emilia-romagna.it/issues/28747) Gestione upload multipart large data object con RestTemplate (invocazione microservizio di verifica firma) 
- [#28571](https://parermine.regione.emilia-romagna.it/issues/28571)  Correzione verifica-controllo hash componente versato
- [#28565](https://parermine.regione.emilia-romagna.it/issues/28565) Correzione log errore in caso di problemi al salvataggio della sessione di versamento
- [#28449](https://parermine.regione.emilia-romagna.it/issues/28449) Correzione JPQL su viste in fase di persistenza URN unità documentaria
- [#28032](https://parermine.regione.emilia-romagna.it/issues/28032) Risoluzione errore 666 in caso di mancanza tag opzionale Configurazione
- [#27471](https://parermine.regione.emilia-romagna.it/issues/27471) Mancato funzionamento parametro FL_OBBL_DATA

## 4.5.0 (28-02-2023)

### Novità: 1
- [#25924](https://parermine.regione.emilia-romagna.it/issues/25924) Salvataggio diretto su object storage

### Bugfix: 3
- [#28094](https://parermine.regione.emilia-romagna.it/issues/28094) Correzione di conversione nel passaggio dei parametri per il salvataggio sincrono
- [#28036](https://parermine.regione.emilia-romagna.it/issues/28036)  Correzione su gestione caratteri non validi nelle risposte alle chiamate dei servizi
- [#27463](https://parermine.regione.emilia-romagna.it/issues/27463) gestione caso in cui TSA non contenga un certificato corretto

### Novità: 1
- [#28005](https://parermine.regione.emilia-romagna.it/issues/28005)  Implementazione versamento diretto su Object storage

## 4.4.0 (23-11-2022)

### Novità: 1
- [#27534](https://parermine.regione.emilia-romagna.it/issues/27534) aggiornamento libreria dss 5.10

## 4.2.1.2 (15-09-2022)

### Bugfix: 2
- [#27681](https://parermine.regione.emilia-romagna.it/issues/27681) Correzione persistenza date e timestamp con Hibernate e JPA 2.2
- [#27629](https://parermine.regione.emilia-romagna.it/issues/27629) correzione dell'ERRORE FILESYS-003-00 su versamento DPI

## 4.2.1.1 (09-09-2022)

### Bugfix: 1
- [#27617](https://parermine.regione.emilia-romagna.it/issues/27617) Correzione dell'errore Hibernate nel salvataggio di un documento

## 3.2.1 (02-08-2022)

### Bugfix: 1
- [#27513](https://parermine.regione.emilia-romagna.it/issues/27513) Correzione problema di concorrenza nella creazione degli elenchi

## 3.2.0 (31-05-2022)

### Novità: 2
- [#27249](https://parermine.regione.emilia-romagna.it/issues/27249) Aggiornamento libreria DSS 5.9
- [#27048](https://parermine.regione.emilia-romagna.it/issues/27048) Ottimizzazione job di Creazione elenchi di versamento - Producer coda IN_ATTESA_SCHED

## 3.1.6.1 (24-05-2022)

### Bugfix: 1
- [#27272](https://parermine.regione.emilia-romagna.it/issues/27272) Correzione procedura di gestione errori della verifica firma EIDAS nei casi di firme non valide che non contengono certificati di firma

## 3.1.6 (10-05-2022)

### Bugfix: 3
- [#27003](https://parermine.regione.emilia-romagna.it/issues/27003) Correzione dell'XSD WSRequestUnico.xsd delle specifiche di versamento UD 1.4
- [#26836](https://parermine.regione.emilia-romagna.it/issues/26836) Correzione popolamento soggetto della CA
- [#26757](https://parermine.regione.emilia-romagna.it/issues/26757) Correzione della risposta in caso di controllo certificato non consistente

### Novità: 1
- [#26624](https://parermine.regione.emilia-romagna.it/issues/26624) Modifica gestione dei log level sui processi che impattano la verifica firma

## 3.1.5 (27-04-2022)

### Novità: 1
- [#26508](https://parermine.regione.emilia-romagna.it/issues/26508) Gestione nuove codifiche profili per versamento fascicolo

## 4.1.4.1 (11-04-2022)

### Bugfix: 1
- [#27093](https://parermine.regione.emilia-romagna.it/issues/27093) Correzione problema di chiusura connessione
## 4.1.4 (28-03-2022)

### Bugfix: 3
- [#26930](https://parermine.regione.emilia-romagna.it/issues/26930) Risoluzione del problema della mancata chiusura di connessioni al DB
- [#26850](https://parermine.regione.emilia-romagna.it/issues/26850) Errore mancata gestione nel versamento di un fascicolo
- [#26846](https://parermine.regione.emilia-romagna.it/issues/26846) Errore in caso di aggiornamento metadati con USERID errato

### Novità: 1
- [#26982](https://parermine.regione.emilia-romagna.it/issues/26982) aggiornamento versione hibernate con versione SACER WS 3.1.4

## 3.1.4 (24-03-2022)

### Bugfix: 1
- [#26746](https://parermine.regione.emilia-romagna.it/issues/26746) Errore nell'xsd di validazione della response

## 4.1.3.2 (23-02-2022)

### Bugfix: 2
- [#26819](https://parermine.regione.emilia-romagna.it/issues/26819) Errore 666 in SACER WS TEST
- [#26493](https://parermine.regione.emilia-romagna.it/issues/26493) Correzione errore 666P per disallineamento tra XSD e DB

## 4.1.3.1 (18-02-2022)

### Bugfix: 1
- [#26804](https://parermine.regione.emilia-romagna.it/issues/26804) Errore 666p in SACER WS TEST

## 4.1.3 (09-02-2022)

### Novità: 2
- [#26262](https://parermine.regione.emilia-romagna.it/issues/26262) Allineamento alla versione 3.1.3 (eclipselink)
- [#21190](https://parermine.regione.emilia-romagna.it/issues/21190) Migrazione Hibernate

## 3.1.3 (31-01-2022)

### Novità: 1
- [#26662](https://parermine.regione.emilia-romagna.it/issues/26662) aggiornamento librerie obsolete primo quadrimestre 2021

## 3.1.2 (13-01-2022)

### Bugfix: 1
- [#26579](https://parermine.regione.emilia-romagna.it/issues/26579) Correzione salvataggio nuovo profilo ud
## 3.1.1 (30-12-2021)

### Bugfix: 1
- [#26515](https://parermine.regione.emilia-romagna.it/issues/26515) Correzione rapporto di versamento indirizzo ip sistema versante

## 3.1.0 (20-12-2021)

### Novità: 1
- [#26169](https://parermine.regione.emilia-romagna.it/issues/26169) Introduzione gestione del nuovo profilo normativo

## 3.0.9 (20-12-2021)

### Bugfix: 2
- [#26453](https://parermine.regione.emilia-romagna.it/issues/26453) Gestione errore bloccante per formato file standard non presente
- [#26452](https://parermine.regione.emilia-romagna.it/issues/26452) Correzione gestione campo note del profilo generale su versamento fascicoli

## 3.0.8 (02-12-2021)

### Bugfix: 2
- [#26393](https://parermine.regione.emilia-romagna.it/issues/26393) Gestione errore codificato quando formato file standard non presente
- [#26369](https://parermine.regione.emilia-romagna.it/issues/26369) Correzione gestione date come parametro di query

## 3.0.7 (02-12-2021)

### Bugfix: 1
- [#26355](https://parermine.regione.emilia-romagna.it/issues/26355) Correzione errore controlli formali 

### Novità: 1
- [#26266](https://parermine.regione.emilia-romagna.it/issues/26266) Gestione delle casistiche di eccezioni generiche su recepimento delle riposta da servizio verifica firma

## 3.0.6.1 (26-11-2021)

### Bugfix: 1
- [#26323](https://parermine.regione.emilia-romagna.it/issues/26323) Correzione gestione date come parametro di query 
## 3.0.6

### EVO: 2
- [#25667](https://parermine.regione.emilia-romagna.it/issues/25667) Implementazione versione 1.5 dei servizi di versamento sincrono
- [#21932](https://parermine.regione.emilia-romagna.it/issues/21932) Completamento aggiornamento verifica firme 

### Bugfix: 1
- [#26013](https://parermine.regione.emilia-romagna.it/issues/26013) Verifica su riferimento temporale

### Novità: 6
- [#26270](https://parermine.regione.emilia-romagna.it/issues/26270) Gestire più di una marca temporale su un singolo componente
- [#25288](https://parermine.regione.emilia-romagna.it/issues/25288) Calcolo nuovo URN relativo al SIP nel versamento fascicolo
- [#24644](https://parermine.regione.emilia-romagna.it/issues/24644) Gestione errore codificato quando formato file standard non presente
- [#24557](https://parermine.regione.emilia-romagna.it/issues/24557) Aggiunta campo Utente nella versione 1.5 dei ws di versamento
- [#22406](https://parermine.regione.emilia-romagna.it/issues/22406) Gestione degli ocsp in fase di verifica firme
- [#21971](https://parermine.regione.emilia-romagna.it/issues/21971) Inserimento nuovi tag nell'Esito versamento

## 3.0.5 (19-11-2021)

### Bugfix: 1
- [#26281](https://parermine.regione.emilia-romagna.it/issues/26281) Disattivato aggiornamento di UD in stato IN VOLUME DI CONSERVAZIONE

## 3.0.4 (15-11-2021)

### Novità: 1
- [#24651](https://parermine.regione.emilia-romagna.it/issues/24651) Tracciamento nei log del parametro SimulaSalvataggioDatiInDB a true

## 3.0.3 (05-11-2021)

### Bugfix: 1
- [#26201](https://parermine.regione.emilia-romagna.it/issues/26201) correzione della procedura di versamento nel caso di  N componenti di cui alcuni firmati ed altri no

## 3.0.1.1 (26-10-2021)

### Bugfix: 1
- [#26119](https://parermine.regione.emilia-romagna.it/issues/26119) correzione della procedura di versamento nel caso di  N componenti di cui alcuni firmati ed altri no
## 3.0.2 (21-10-2021)

### Bugfix: 1
- [#25961](https://parermine.regione.emilia-romagna.it/issues/25961) Modificare urn Aggiornamento metadati UD

### Novità: 1
- [#25347](https://parermine.regione.emilia-romagna.it/issues/25347) Generazione report di verifica firma su object storage

## 3.0.1

### Bugfix: 3
- [#26030](https://parermine.regione.emilia-romagna.it/issues/26030) Correzione mancata presenza del file del certificato della CA e/o del firmatario e/o della CRL in fase di verifica firma del componente
- [#26016](https://parermine.regione.emilia-romagna.it/issues/26016) Correzione della gestione degli esiti in mancanza di  un formato standard
- [#23623](https://parermine.regione.emilia-romagna.it/issues/23623) Gestione disabilitazione verifica firma nella risposta 1.4

### Novità: 1
- [#25642](https://parermine.regione.emilia-romagna.it/issues/25642) Ottimizzazione meccanismo di retry 

## 3.0.0 (08-10-2021)

### Novità: 1
- [#26017](https://parermine.regione.emilia-romagna.it/issues/26017) unificazione contesto applicativo di sacerws

## 2.3.6-EIDAS-1.8 (30-09-2021)

### Bugfix: 4
- [#25213](https://parermine.regione.emilia-romagna.it/issues/25213) Correzione esito verifica in caso di utilizzo della data di firma come riferimento temporale
- [#23714](https://parermine.regione.emilia-romagna.it/issues/23714) Errata valorizzazione del riferimento temporale usato nella response al versamento
- [#23701](https://parermine.regione.emilia-romagna.it/issues/23701) Formato xades non conforme restituisce come formato asics
- [#23700](https://parermine.regione.emilia-romagna.it/issues/23700) Esiti non attesi per verifica su CRL e verifica con controllo crittografico disabilitato

## 2.3.6-EIDAS-1.7 (10-08-2021)

### Bugfix: 1
- [#25436](https://parermine.regione.emilia-romagna.it/issues/25436) Correzione dipendenze librerie JAXB e JAVAX ACTIVATION

## 2.3.6 (10-08-2021)

### Novità: 1
- [#23176](https://parermine.regione.emilia-romagna.it/issues/23176) Calcolo nuovi URN relativi ai SIP

## 2.3.5-EIDAS.1.6 (07-07-2021)

### Novità: 1
- [#25312](https://parermine.regione.emilia-romagna.it/issues/25312) Aggiunta parametro per abilitare la generazione dei report verifica firma 

## 2.3.5-EIDAS.1.5 (18-05-2021)

### Bugfix: 3
- [#24880](https://parermine.regione.emilia-romagna.it/issues/24880) Correzione esito verifica in caso di marca temporale
- [#24362](https://parermine.regione.emilia-romagna.it/issues/24362) Correzione valore persistito legato alla URL della CRL
- [#24322](https://parermine.regione.emilia-romagna.it/issues/24322) Correzione errore gestione esito WARNING con FL_ABILITA_CONTR_NON_FIRMATI = true

## 2.3.5 (18-05-2021)

### Bugfix: 5
- [#24842](https://parermine.regione.emilia-romagna.it/issues/24842) Correzione naming convention del componente/sottocomponente per versamenti di tipo FILE
- [#24446](https://parermine.regione.emilia-romagna.it/issues/24446) Correzione gestione dell'errore relativo all'ordine presentazione di un sottocomponente.
- [#24089](https://parermine.regione.emilia-romagna.it/issues/24089) Generazione javadoc
- [#22948](https://parermine.regione.emilia-romagna.it/issues/22948) Aggiornamento di UD in stato IN VOLUME DI CONSERVAZIONE
- [#22947](https://parermine.regione.emilia-romagna.it/issues/22947) Aggiornamento di UD in stato AIP FIRMATO

### Novità: 2
- [#22978](https://parermine.regione.emilia-romagna.it/issues/22978) Aggiunta di ulteriori  informazioni nel rapporto di versamento.
- [#15448](https://parermine.regione.emilia-romagna.it/issues/15448) Aggiunta memorizzazione dell'indirizzo IP e del sistema versante dell'utente versatore
