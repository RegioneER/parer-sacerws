---
title: "Configurazione SacerWS"
---
# Configurazione Jboss EAP 7.3

## Versioni 

| Vers. Doc | Vers. SacerWS  | Modifiche  |
| -------- | ---------- | ---------- |
| 1.0.0 |  | Versione iniziale del documento  |

## Datasource XA

### Console web

`Configuration > Connector > datasources`

Scegliere **XA DATASOURCES** e premere 

`Add`

Si apre un wizard in 3 passaggi
1. Aggiungere gli attributi del datasource: Nome=**SacerVersPool** e JNDI=**java:jboss/datasources/SacerVersDs**
2. Selezionare il driver **ojdbc8** (predisposto durante la configurazione generale di Jboss) e impostare **oracle.jdbc.xa.client.OracleXADataSource** come XA Data Source Class;
3. Impostare gli attributi della connessione, ad esempio *URL* 

#### JBoss CLI

```bash
xa-data-source add --name=SacerVersPool --jndi-name=java:jboss/datasources/SacerVersDs --xa-datasource-properties={"URL"=>"jdbc:oracle:thin:@parer-vora-b03.ente.regione.emr.it:1521/PARER19S.ente.regione.emr.it"} --user-name=SACER --password=exit  --driver-name=ojdbc8 --max-pool-size=64 --spy=true --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter --stale-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker --statistics-enabled=true --use-ccm=true --use-fast-fail=true --validate-on-match=true --flush-strategy=FailingConnectionOnly --background-validation=false --min-pool-size=8 --enabled=true --allow-multiple-users=false --connectable=false  --set-tx-query-timeout=false --share-prepared-statements=false --track-statements=NOWARN
```

### Transaction service 

Lo schema dell'applicazione ha bisogno delle seguenti grant su Oracle.

```sql
GRANT SELECT ON sys.dba_pending_transactions TO SACER;
GRANT SELECT ON sys.pending_trans$ TO SACER;
GRANT SELECT ON sys.dba_2pc_pending TO SACER;
GRANT EXECUTE ON sys.dbms_xa TO SACER;
```

La procedura è descritta nella documentazione standard di JBoss EAP 7.3

https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.3/html/configuration_guide/datasource_management#vendor_specific_xa_recovery

## Configurazione Servizio JMS

Per la configurazione del subsystem si rimanda alla documentazione generale di JBoss EAP 6.4 del ParER.  
Una volta fatto è necessario impostare le risorse JMS.

### Configurazione Risorse JMS e Nomi JNDI

#### Console web

Configuration > Messaging > Destinations` 

Andare in `View` sul **default**  quindi 

`Queues/Topics > Queue`

Cliccare su 

`Add` 

e aggiungere le seguenti destinazioni 

Name | JNDI 
--- | --- 
ElenchiDaElabQueue | java:/jms/queue/ElenchiDaElabQueue |

#### JBoss CLI

```bash
jms-queue add --queue-address=ElenchiDaElabQueue --entries=[java:/jms/queue/ElenchiDaElabQueue]
```

### Bean pool per gli MDB

#### Console web

Configuration > Container > EJB 3 > BEAN POOLS`

Aggiungere i seguenti Bean Pools  

Name | Max Pool Size | Timeout | Timeout unit |
--- | --- | --- | --- |
coda-elenchi-da-elab-pool | 3 | 5 | MINUTES |

#### JBoss CLI

```bash
/subsystem=ejb3/strict-max-bean-instance-pool=coda-elenchi-da-elab-pool:add(max-pool-size=5, timeout=5, timeout-unit="MINUTES")
```



## Logging profile

### JDBC custom handler
Assicurarsi di aver installato il modulo ApplicationLogCustomHandler (Vedi documentazione di configurazione di Jboss EAP 7.3).

Configurare un custom handler nel subsystem **jboss:domain:logging:1.5**.

```xml
<subsystem xmlns="urn:jboss:domain:logging:1.5">
    <!-- ... --> 
    <custom-handler name="sacerws_jdbc_handler" class="it.eng.tools.jboss.module.logger.ApplicationLogCustomHandler" module="it.eng.tools.jboss.module.logger">
        <level name="INFO"/>
        <formatter>
            <named-formatter name="PATTERN"/>
        </formatter>
        <properties>
            <property name="fileName" value="sacerws_jdbc.log"/>
            <property name="deployment" value="sacerws"/>
        </properties>
    </custom-handler>
    <!-- ... -->
</subsystem>
```

I comandi CLI

```bash 
/subsystem=logging/custom-handler=sacerws_jdbc_handler:add(class=it.eng.tools.jboss.module.logger.ApplicationLogCustomHandler,module=it.eng.tools.jboss.module.logger,level=INFO)
/subsystem=logging/custom-handler=sacerws_jdbc_handler:write-attribute(name=named-formatter,value=PATTERN)
/subsystem=logging/custom-handler=sacerws_jdbc_handler:write-attribute(name=properties,value={fileName=>"sacerws_jdbc.log", deployment=>"sacerws"})
```

Associare l'handler ai logger **jboss.jdbc.spy** e **org.hibernate**, sempre nel subsystem **jboss:domain:logging:1.5**.

```xml
<subsystem xmlns="urn:jboss:domain:logging:1.5">
    <!-- ... -->
    <logger category="jboss.jdbc.spy" use-parent-handlers="false">
        <level name="DEBUG"/>
        <filter-spec value="match(&quot;Statement|prepareStatement&quot;)"/>
        <handlers>
            <handler name="sacerws_jdbc_handler"/>
        </handlers>
    </logger>
    <logger category="org.hibernate" use-parent-handlers="false">
        <level name="WARNING"/>
        <handlers>
            <handler name="sacerws_jdbc_handler"/>
        </handlers>
    </logger>
    <!-- ... -->
</subsystem>
```

I comandi CLI

```bash
/subsystem=logging/logger=org.hibernate:add-handler(name=sacerws_jdbc_handler)
/subsystem=logging/logger=jboss.jdbc.spy:add-handler(name=sacerws_jdbc_handler)
```

### Profilo di SacerWS

#### JBoss CLI

```bash
/subsystem=logging/logging-profile=SACERWS:add()
/subsystem=logging/logging-profile=SACERWS/periodic-rotating-file-handler=sacerws_handler:add(level=INFO,formatter="%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%E%n",file={path="sacerws.log",relative-to="jboss.server.log.dir"},suffix=".yyyy-MM-dd",append=true)
/subsystem=logging/logging-profile=SACERWS/size-rotating-file-handler=sacerws_tx_connection_handler:add(level=DEBUG,formatter="%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%E%n",file={path="sacerws_conn_handler.log",relative-to="jboss.server.log.dir"},append=true,max-backup-index=1,rotate-size="256m")
/subsystem=logging/logging-profile=SACERWS/logger=org.jboss.jca.core.connectionmanager.listener.TxConnectionListener:add(level=DEBUG,handlers=[sacerws_tx_connection_handler],use-parent-handlers=false)
/subsystem=logging/logging-profile=SACERWS/root-logger=ROOT:add(level=INFO,handlers=[sacerws_handler])
/subsystem=logging/logging-profile=SACERWS/logger=org.springframework:add(level=ERROR,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=org.opensaml:add(level=ERROR,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=es.mityc:add(level=INFO,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=it.eng.crypto:add(level=INFO,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=it.eng.parer.crypto:add(level=INFO,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=it.eng.parer.volume:add(level=INFO,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=it.eng.parer.ws:add(level=INFO,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=it.eng.parer.restWS:add(level=INFO,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=it.eng.parer.admin:add(level=INFO,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=it.eng.parer.web:add(level=INFO,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=it.eng.spagoLite:add(level=INFO,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=it.eng.parer.ws.utils.AvanzamentoWs:add(level=OFF,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=org.exolab.castor.xml.NamespacesStack:add(level=OFF,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=org.exolab.castor.xml.EndElementProcessor:add(level=ERROR,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=org.hibernate:add(level=ERROR,use-parent-handlers=true)
/subsystem=logging/logging-profile=SACERWS/logger=jboss.jdbc.spy:add(level=ERROR,use-parent-handlers=true)
```

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

## System properties

### Console web

`Configuration > System properties`

Impostare le seguenti properties

Chiave | Valore di esempio | Descrizione
--- | --- | ---
reportvf.aws.accessKeyId | <accessKeyId_object_storage> | Access Key id delle credenziali S3 per l’accesso all’object storage per i report di verifica firma.
reportvf.aws.secretKey | <secretKey_object_storage> | Secret Key delle credenziali S3 per l’accesso all’object storage per i report di verifica firma.

### jboss cli

```bash
/system-property=reportvf.aws.accessKeyId:add(value="<accessKeyId_object_storage>")
/system-property=reportvf.aws.secretKey:add(value="<secretKey_object_storage>")
```

## Regole di Rewrite

Tutte le risorse esposte dal contesto */sacerws* **NON** devono essere accessibili ad eccezione di:
 - https://parer.regione.emilia-romagna.it/sacerws/VersamentoFascicoloSync ;
 - https://parer.regione.emilia-romagna.it/sacerws/VersamentoSync ;
 - https://parer.regione.emilia-romagna.it/sacerws/AggiuntaAllegatiSync ;
 - https://parer.regione.emilia-romagna.it/sacerws/VersamentoMultiMedia .

## Object storage: configurazione AWS Access Key ID e  Secret Access Key  

Sono attivabili da applicazione, meccanismi di accesso a file depositati su object storage, nello specifico è possibile configuare alcune system properties che permettono all'applicazione di recuperare in modalità chiave/valore le credenziali di accesso necessarie per l'interazione con l'object storage secondo lo standard AWS S3 (https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html)
La chiave da impostare dipende dalla configurazione presente su database, vedere nello specifico la tabella **DEC_CONFIG_OBJECT_STORAGE**, nella quale potranno essere configurate le chiavi presenti tra le system properties, se ne riporta di seguito un esempio:

```bash
batch

/system-property=sip-w.aws.accessKeyId:add(value="$accessKeyId")
/system-property=sip-w.aws.secretKey:add(value="$secretKey")

run-batch

```

nel caso specifico dello script sopra riportato, le chiavi interessate sono : **sip-w.aws.accessKeyId** e **sip-w.aws.secretKey**; rispettivamente configurate sulla tabella citata in precedenza.

Esempio di configuazione su database 


|  ID_DEC_CONFIG_OBJECT_STORAGE | ID_DEC_BACKEND   |  DS_VALORE_CONFIG_OBJECT_STORAGE | TI_USO_CONFIG_OBJECT_STORAGE  | NM_CONFIG_OBJECT_STORAGE  |  DS_DESCRIZIONE_CONFIG_OBJECT_STORAGE |
|---|---|---|---|---|---|
|  1 |  2 | ACCESS_KEY_ID_SYS_PROP  | sip-w.aws.accessKeyId  | WRITE_SIP   |   Nome della system property utilizzata per l'access key id per il bucket dei sip in sola scrittura |
|  2 | 2  | SECRET_KEY_SYS_PROP  | sip-w.aws.secretKey  | WRITE_SIP  | Nome della system property utilizzata per la secret key per il bucket dei sip in sola scrittura |

Nota: la FK (chiave esterna) legata al valore presente su colonna ID_DEC_BACKEND, dipende dalla configurazione presente su DEC_BACKEND.

