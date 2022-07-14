---
title: "Configurazione SacerWS"
---
# Configurazione Jboss EAP 6.4

## Versioni 

| Vers. Doc | Vers. SacerWS  | Modifiche  |
| -------- | ---------- | ---------- |
| 1.0.0 | 2.3.4 | Versione iniziale del documento  |

## Datasource XA

Per configurare il datasource dell'applicativo dalla console web di amministrazione bisogna andare su

`Configuration > Connector > datasources`

Scegliere **XA DATASOURCES** e premere 

`Add`

Si apre un wizard in 3 passaggi
1. Aggiungere gli attributi del datasource: Nome=**SacerPool** e JNDI=**java:/jdbc/SacerDs**
2. Selezionare il driver **ojdbc8** (predisposto durante la configurazione generale di Jboss) e impostare **oracle.jdbc.xa.client.OracleXADataSource** come XA Data Source Class;
3. Impostare gli attributi della connessione, ad esempio *URL* 

### Configurazione del transaction service 

Lo schema dell'applicazione ha bisogno delle seguenti grant su Oracle.

```sql
GRANT SELECT ON sys.dba_pending_transactions TO SACER;
GRANT SELECT ON sys.pending_trans$ TO SACER;
GRANT SELECT ON sys.dba_2pc_pending TO SACER;
GRANT EXECUTE ON sys.dbms_xa TO SACER;
```

La procedura è descritta nella documentazione standard di JBoss EAP 6.4

https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-XA_Datasources.html#Create_an_XA_Datasource_with_the_Management_Interfaces

## Configurazione Servizio JMS

Per la configurazione del subsystem si rimanda alla documentazione generale di JBoss EAP 6.4 del ParER.  
Una volta fatto è necessario impostare le risorse JMS.

### Configurazione Risorse JMS e Nomi JNDI

#### Configurazione tramite interfaccia web

`Configuration > Messaging > Destinations` 

Andare in `View` sul **default**  quindi 

`Queues/Topics > Queue`

Cliccare su 

`Add` 

e aggiungere la seguente destinazione 

Name | JNDI 
--- | --- 
ElenchiDaElabInAttesaSchedQueue | java:/jms/queue/ElenchiDaElabInAttesaSchedQueue |

#### Configurazione tramite CLI

```bash
jms-queue --profile={my-profile} add --queue-address=ElenchiDaElabInAttesaSchedQueue --entries=[java:/jms/queue/ElenchiDaElabInAttesaSchedQueue]
```

Sostiture {my-profile} con la keyword adeguata. 

### Bean pool per gli MDB

#### Configurazione tramite interfaccia web

`Configuration > Container > EJB 3 > BEAN POOLS`

Aggiungere il seguente Bean Pool

Name | Max Pool Size | Timeout | Timeout unit |
--- | --- | --- | --- |
coda-elenchi-da-elab-in-attesa-sched-pool | 5 | 5 | MINUTES |

#### Configurazione tramite CLI

```bash
/profile={my-profile}/subsystem=ejb3/strict-max-bean-instance-pool=coda-elenchi-da-elab-in-attesa-sched-pool:add(max-pool-size=3, timeout=5, timeout-unit="MINUTES")
``` 

Sostiture {my-profile} con la keyword adeguata. 


## Logging profile

```xml
<logging-profiles>
    <!-- ... -->
    <logging-profile name="SACERWS">
        <periodic-rotating-file-handler name="sacerws_handler" autoflush="true">
            <level name="INFO"/>
            <formatter>
                <pattern-formatter pattern="%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%E%n"/>
            </formatter>
            <file relative-to="jboss.server.log.dir" path="sacerws.log"/>
            <suffix value=".yyyy-MM-dd"/>
            <append value="true"/>
        </periodic-rotating-file-handler>
        <periodic-size-rotating-file-handler name="sacerws_tx_connection_handler" autoflush="true">
            <level name="DEBUG"/>
            <formatter>
                <pattern-formatter pattern="%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%E%n"/>
            </formatter>
            <file relative-to="jboss.server.log.dir" path="sacerws_conn_handler.log"/>
            <append value="true"/>
            <max-backup-index value="1">
            <rotate-size value="256m"/>
        </periodic-size-rotating-file-handler>
        <logger category="org.springframework" use-parent-handlers="true">
            <level name="ERROR"/>
        </logger>
        <logger category="org.opensaml" use-parent-handlers="true">
            <level name="ERROR"/>
        </logger>
        <logger category="es.mityc" use-parent-handlers="true">
            <level name="INFO"/>
        </logger>
        <logger category="it.eng.crypto" use-parent-handlers="true">
            <level name="INFO"/>
        </logger>
        <logger category="it.eng.parer.crypto" use-parent-handlers="true">
            <level name="INFO"/>
        </logger>
        <logger category="it.eng.parer.volume" use-parent-handlers="true">
            <level name="INFO"/>
        </logger>
        <logger category="it.eng.parer.ws" use-parent-handlers="true">
            <level name="INFO"/>
        </logger>
        <logger category="it.eng.parer.restWS" use-parent-handlers="true">
            <level name="INFO"/>
        </logger>
        <logger category="it.eng.parer.admin" use-parent-handlers="true">
            <level name="INFO"/>
        </logger>
        <logger category="it.eng.parer.web" use-parent-handlers="true">
            <level name="INFO"/>
        </logger>
        <logger category="it.eng.spagoLite" use-parent-handlers="true">
            <level name="INFO"/>
        </logger>
        <logger category="it.eng.parer.ws.utils.AvanzamentoWs"use-parent-handlers="true">
            <level name="OFF"/>
        </logger>
        <logger category="org.exolab.castor.xml.NamespacesStack" use-parent-handlers="true">
            <level name="OFF"/>
        </logger>
        <logger category="org.exolab.castor.xml.EndElementProcessor" use-parent-handlers="true">
            <level name="ERROR"/>
        </logger>
        <logger category="org.exolab.castor.xml.EndElementProcessor" use-parent-handlers="true">
            <level name="ERROR"/>
        </logger>
                <logger category="stdout" use-parent-handlers="true">
            <level name="OFF"/>
        </logger>
        <logger category="org.jboss.jca.core.connectionmanager.listener.TxConnectionListener" use-parent-handlers="true">
            <level name="DEBUG"/>
            <handlers>
                <handler name="sacerws_tx_connection_handler"/>
            </handlers>
        </logger>
        <root-logger>
            <level name="INFO"/>
            <handlers>
                <handler name="sacerws_handler"/>
            </handlers>
        </root-logger>
    </logging-profile>
    <!-- ... -->
</logging-profiles>
```

Chiave | Valore di esempio | Descrizione
--- | --- | ---
reportvf.aws.accessKeyId | <accessKeyId_object_storage> | Access Key id delle credenziali S3 per l’accesso all’object storage per i report di verifica firma.
reportvf.aws.secretKey | <secretKey_object_storage> | Secret Key delle credenziali S3 per l’accesso all’object storage per i report di verifica firma.


## Regole di Rewrite

Tutte le risorse esposte dal contesto */sacerws* **NON** devono essere accessibili ad eccezione di:
 - https://parer.regione.emilia-romagna.it/sacerws/VersamentoFascicoloSync ;
 - https://parer.regione.emilia-romagna.it/sacerws/VersamentoSync ;
 - https://parer.regione.emilia-romagna.it/sacerws/AggiuntaAllegatiSync ;
 - https://parer.regione.emilia-romagna.it/sacerws/VersamentoMultiMedia .
