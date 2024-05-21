---
title: "Configurazione di JBoss EAP 7.4"
---
# Configurazione JBoss EAP 7.4

## Introduzione

Il presente documento definisce le configurazioni da apportare all'application server JBoss  EAP 7.4 necessarie alla corretta esecuzione degli applicativi sviluppati da ParER (Polo Archivistico Regione Emilia-Romagna).

In questo documento la cartella radice di JBoss verrà indicata spesso con *${JBOSS_HOME}*. Tutti i percorsi su filesystem (a meno che non sia indicato esplicitamente) sono relativi a questa cartella.

In ogni caso per maggiori dettagli si rimanda alla [Configuration guide](#riferimenti).


## Versioni

| Versione | Modifiche |
| -------- | ---------- |
| 1.0.0    | Versione iniziale del documento  |
| 1.0.1    | Aggiunte delle system properties |
| 1.0.2    | Aggiornamento JBoss EAP 7.4 e ojdbc 11|

## JDK

La JDK di riferimento utilizzata dalle applicazioni descritte in questo documento è la OpenJDK versione 11.

## Aggiunta amministratore application server

Per poter effettuare operazioni amministrative all'interno dell'installazione di JBoss può essere utile creare almeno un utente afferente al ruolo ManagementRealm. In ambiente standalone tale operazione può essere effettuata tramite lo script `bin/add-user.sh` (o .bat per windows).
Si può in alternativa decidere di installare e gestire direttamente con l'utente root di S.O.

Ecco un esempio dell'esecuzione dello script in cui l'utenza inserita è `admin.jboss.eap` e password `secretpassword`.  

```bash
$ ./opt/jboss-eap-7.4.0/bin/add-user.sh
What type of user do you wish to add?
a) Management User (mgmt-users.properties)
b) Application User (application-users.properties)
(a):
 Enter the details of the new user to add.
 Using realm 'ManagementRealm' as discovered from the existing property 
files.
 Username : admin.jboss.eap
 Password requirements are listed below. To modify these restrictions 
 edit the adduser.properties configuration file.
 -- The password must not be one of the following restricted values 
    {root, admin, administrator}
 -- The password must contain at least 8 characters, 1 alphabetic 
    character(s), 1 digit(s), 1 non-alphanumeric symbol(s)
 -- The password must be different from the username
 Password :
 Re-enter Password :
 What groups do you want this user to belong to?
 (Please enter a comma separated list, or leave blank for none)[ ]:
 About to add user 'admin.jboss.eap' for realm 'ManagementRealm'
 Is this correct yes/no? yes
 Added user 'admin.jboss.eap' to file
 '/opt/jboss-eap-7.4.0/standalone/configuration/mgmtusers.properties'
 Added user 'admin.jboss.eap' to file 
 '/opt/jboss-eap-7.4.0/domain/configuration/mgmtusers.properties'
 Added user 'admin.jboss.eap' with groups to file 
 '/opt/jboss-eap-7.4.0/standalone/configuration/mgmt-groups.properties'
 Added user 'admin.jboss.eap' with groups to file 
 '/opt/jboss-eap-7.4.0/domain/configuration/mgmt-groups.properties'
 Is this new user going to be used for one AS process to connect to 
 another AS process?
 e.g. for a slave host controller connecting to the master or for a 
 Remoting connection for server to server EJB calls.
 yes/no? y
 To represent the user add the following to the server-identities 
 definition <secret value="c2VjcmV0cGFzc3dvcmQ=" /></code>
```

## Applicazione delle patch 

È opportuno applicare tutte le patch disponibili al momento dell'installazione, si trovano le cumulative patch (CP) sul [sito RedHat](https://access.redhat.com/jbossnetwork/restricted/listSoftware.html?product=appplatform&downloadType=distributions).

Per le istruzioni operative si rimanda al capitolo 2 della [Patching guide](#riferimenti).

## Configurazione Standalone

La versione standalone di JBoss offre 4 profili pre-configurati:

 - **standalone.xml**: supporta Java EE Web profile più alcune estensioni come servizi web
RESTFul e invocazioni remote a EJB3. É il profilo predefinito;
 - **standalone-full.xml**: Supporta Java EE Full-Profile e tutte le funzionalità lato server senza il
clustering. Questo profilo supporta anche le code JMS;
 - **standalone-ha.xml**: profilo predefinito + clustering;
 - **standalone-full-ha.xml**: Java EE Full-Profile + clustering.

 Ci sono due modi per attivare uno dei profili:

 - **Specificare a linea di comando il file da usare**
 ```bash
${JBOSS_HOME}/bin/standalone.sh --server-config=<file-xml-profilo-da-usare>
 ```

 - **Sovrascrivere il file standalone.xml**  
 Se non diversamente specificato viene utilizzato il file *standalone.xml*, pertanto sovrascrivendolo si attiva il profilo desiderato, anche se si tratta di una gestione che potrebbe creare confusione sui contenuti dei file. 

### Ambiente di Sviluppo locale 

 Il profilo da usare per l'ambiente di sviluppo locale è **full** se si utilizzano le code, altrimenti è sufficiente quello standard. 

### Ambienti Snapshot, Test, Pre-produzione e Produzione 

 In ParER, gli ambienti server di snapshot, test, pre-produzione e produzione vanno configurati sempre in modalità standalone ma con il profilo **full-ha**.

## Configurazione JDBC

### Driver Oracle

Il DBMS di riferimento è Oracle alla versione 19c e la versione del driver Oracle JDBC è la 11. 
L'installazione del driver è spiegata dettagliatamente al paragrafo 12.2 della [Configuration guide](#riferimenti).

#### Installazione

Una volta scaricato l'ojdbc11.jar va installato tramite la Command Line Interface (CLI) di JBoss (${JBOSS_HOME}/bin/jboss-cli.sh) a server acceso:

```bash
module add --name=com.oracle.ojdbc11 --resources=ojdbc11.jar --dependencies=javax.api,javax.transaction.api
```

#### Registrazione 

Per rendere disponibile il driver ai datasource è necessario eseguire l'installazione dalla CLI di JBoss; non è
possibile eseguire tale operazione dalla console di amministrazione web.  

Seguire i seguenti passaggi **a server acceso**:

```bash
/subsystem=datasources/jdbc-driver=ojdbc11:add(driver-name=ojdbc11, driver-module-name=com.oracle.ojdbc11, driver-xa-datasource-class-name=oracle.jdbc.xa.client.OracleXADataSource,driver-class-name=oracle.jdbc.OracleDriver)
```

### Configurazione datasource / connection pool

Per configurare i datasource dell'applicativo dalla console di amministrazione bisogna andare su

*Configuration > Subsystem > Datasources & Drivers > Datasources*

#### Configurazione datasource non XA

Cliccando su **(+)** si avvia la procedura guidata per la configurazione del datasource che, nel nostro caso, sono tutti **Oracle**.  
Per le configurazioni specifiche si rimanda alla documentazione delle singole applicazioni.

#### Configurazione datasource XA

In ambiente ParER solo Sacer e SacerPing usano dei *datasource XA*.  
La configurazione è leggermente differente rispetto a quella di un datasource non XA perché, 
oltre al driver **ojdbc11**, ha anche le XA properties.  
Le configurazioni specifiche sono disponibili nella documentazione dell'applicazione. 

### Configurazione del transaction service

Per i datasource di tipo XA l'owner dello schema ha bisogno delle seguenti grant su Oracle verso l'utenza
del database dell'applicazione che usa il datasource XA:

```sql
GRANT SELECT ON sys.dba_pending_transactions TO <UTENTE_DB_APPLICAZIONE>;
GRANT SELECT ON sys.pending_trans$ TO <UTENTE_DB_APPLICAZIONE>;
GRANT SELECT ON sys.dba_2pc_pending TO <UTENTE_DB_APPLICAZIONE>;
GRANT EXECUTE ON sys.dbms_xa TO <UTENTE_DB_APPLICAZIONE>;
```  

In caso di errata assegnazione delle grant l'errore applicativo visualizzato sarà:

```java
WARN  [com.arjuna.ats.jta.logging.loggerI18N]
[com.arjuna.ats.internal.jta.recovery.xarecovery1] 
Local XARecoveryModule.xaRecovery  got XA exception
javax.transaction.xa.XAException, XAException.XAER_RMERR
```
La procedura è descritta al paragrafo 12.8.2 della [Configuration guide](#riferimenti).
## Configurazione code JMS

Il sotto-sistema che si occupa di gestire le code JMS è ActiveMQ/Artemis integrato su JBoss EAP 7.
La configurazione effettuata è di tipo cluster gestita tramite il multicast tra i nodi.
A livello di standalone.xml la configurazione è simile alla seguente:

```xml
<subsystem xmlns="urn:jboss:domain:messaging-activemq:13.0">
    <server name="default">
        <cluster user="parer_test-eap74-jms_user" password="[masked]"/>
        <management jmx-enabled="true"/>
        <journal type="ASYNCIO" min-files="2"/>
        <statistics enabled="${wildfly.messaging-activemq.statistics-enabled:${wildfly.statistics-enabled:false}}"/>
        <security-setting name="#">
            <role name="guest" send="true" consume="true" create-non-durable-queue="true" delete-non-durable-queue="true"/>
            <role name="parer" send="true" consume="true" manage="true"/>
        </security-setting>
        <address-setting name="#" dead-letter-address="jms.queue.DLQ" expiry-address="jms.queue.ExpiryQueue" max-size-bytes="10485760" page-size-bytes="2097152" message-counter-history-day-limit="10" redistribution-delay="1000"/>
        <http-connector name="http-connector" socket-binding="http" endpoint="http-acceptor"/>
        <http-connector name="http-connector-throughput" socket-binding="http" endpoint="http-acceptor-throughput">
            <param name="batch-delay" value="50"/>
        </http-connector>
        <in-vm-connector name="in-vm" server-id="0">
            <param name="buffer-pooling" value="false"/>
        </in-vm-connector>
        <http-acceptor name="http-acceptor" http-listener="default"/>
        <http-acceptor name="http-acceptor-throughput" http-listener="default">
            <param name="batch-delay" value="50"/>
            <param name="direct-deliver" value="false"/>
        </http-acceptor>
        <in-vm-acceptor name="in-vm" server-id="0">
            <param name="buffer-pooling" value="false"/>
        </in-vm-acceptor>
        <jgroups-broadcast-group name="bg-group1" jgroups-cluster="activemq-cluster" connectors="http-connector"/>
        <jgroups-discovery-group name="dg-group1" jgroups-cluster="activemq-cluster"/>
        <cluster-connection name="parer_test-cluster" address="jms" connector-name="http-connector" max-hops="1" discovery-group="dg-group1"/>
        <jms-queue name="ExpiryQueue" entries="java:/jms/queue/ExpiryQueue"/>
        <jms-queue name="DLQ" entries="java:/jms/queue/DLQ"/>
        <connection-factory name="InVmConnectionFactory" entries="java:/ConnectionFactory" connectors="in-vm"/>
        <connection-factory name="RemoteConnectionFactory" entries="java:jboss/exported/jms/RemoteConnectionFactory" connectors="http-connector" ha="true" block-on-acknowledge="true" reconnect-attempts="-1"/>
        <pooled-connection-factory name="activemq-ra" entries="java:/JmsXA java:jboss/DefaultJMSConnectionFactory" connectors="in-vm" transaction="xa"/>
    </server>
</subsystem>

```

## Configurazione IDP

La seguente configurazione deve essere applicata solo se si intende installare e configurare l'IDP fornito da ParER.

## Configurazione del security domain

La gestione del security domain è effettuata direttamente dall'application server ed il comportamento
custom del ParER è definito in un modulo JBoss che deriva dal progetto [JAAS RDBMS](https://github.com/tauceti2/jaas-rdbms).

Creare la cartella *${JBOSS_HOME}/modules/system/layers/base/com/tagish/auth/main*.  
All'interno mettere il file *module.xml* e il relativo jar, personalizzato per il ParER, ottenibile dalla compilazione del progetto [IDP rdbmslogin](https://gitlab.ente.regione.emr.it/parer/idp-rdbmslogin).  

Nel module.xml è molto importante impostare il nome del modulo ojdbc11 precedentemente configurato:

```xml
<?xml version="1.0" encoding="UTF-8"?>
 <module xmlns="urn:jboss:module:1.1" name="com.tagish.auth">
     <resources>
        <resource-root path="commons-codec-1.7.jar"/>
        <resource-root path="idp-jaas-rdbms-0.0.6.jar"/>
     </resources>
     <dependencies>
        <module name="javax.api"/>
        <module name="com.oracle.ojdbc11"/>
     </dependencies>
 </module>
```
 
Per aggiungere il modulo su JBoss EAP 7.4 da console web è sufficiente andare in *Configuration > Subsystem > Security (Legacy) *
  
Cliccare su **(+)** nella colonna *Security domain* e inserire:
 
Name | Cache type
--- | ---
ShibUserPassAuth | default

A questo punto selezionare ShibUserPassAuth, andare su View e nel riquadro *Authentication* premere **Add** ed inserire:

Name | Code | Flag
--- | --- | ---
tagish | com.tagish.auth.DBLogin | required

Dopodiché cliccare su **Edit** e aggiungere i seguenti valori dal **Module Options**:

```properties
userTable=USR_USER
userColumn=NM_USERID
passColumn=CD_PSW
saltColumn=CD_SALT
activeColumn=FL_ATTIVO
expirationColumn=DT_SCAD_PSW
useJndiLookup=true
jndiName=java:jboss/datasources/SiamDs
logTable=SACER_LOG.LOG_LOGIN_FALLITO
logTableCols=NM_APPLIC,NM_USERID,CD_IND_SERVER,TIPO_FALLIMENTO,
    CD_IND_IP_CLIENT,DS_FALLIMENTO,DT_FALLIMENTO,ID_LOGIN_FALLITO
logTableValues=:nmApplic,:nmUser,:cdIndServer,:tipoFallimento,
    :cdIndIpClient,:dsFallimento,:tsFallimento,
    SACER_LOG.SLOG_LOGIN_FALLITO.nextval
serverNameSystemProperty=jboss.node.name
qryParamUserName=nmUser
qryParamDate=tsLogin
```

Poiché da interfaccia non è possibile inserire valori con spazi, bisogna aprire il file standalone.xml e aggiungere questi valori
all'elemento `<security-domain name="ShibUserPassAuth" cache-type="default">`

```xml
<module-option name="qryRetrieveMaxDays" 
    value="SELECT v.ds_valore_param_applic 
            FROM IAM_PARAM_APPLIC p 
            JOIN IAM_VALORE_PARAM_APPLIC v 
            ON (p.id_param_applic=v.id_param_applic) 
            AND p.NM_PARAM_APPLIC = 'MAX_GIORNILOGGER' 
            AND v.TI_APPART = 'APPLIC'"/>
<module-option name="qryRetrieveMaxTry" 
    value="SELECT v.ds_valore_param_applic
     FROM IAM_PARAM_APPLIC p JOIN IAM_VALORE_PARAM_APPLIC v 
     ON (p.id_param_applic=v.id_param_applic) 
     AND p.NM_PARAM_APPLIC = 'MAX_TENTATIVI_FALLITI' 
     AND v.TI_APPART = 'APPLIC'"/>
<module-option name="qryRetrieveLastAccess" 
    value="select T.DT_EVENTO from SACER_LOG.LOG_LOGIN_USER t 
    where T.NM_USERID = :nmUser and T.DT_EVENTO > :tsLogin 
    and T.TIPO_EVENTO = 'LOGIN' order by T.DT_EVENTO desc"/>
<module-option name="qryRetrieveFailedLogins" 
    value="select count(*) from SACER_LOG.LOG_LOGIN_FALLITO t 
    where T.NM_USERID = :nmUser and TIPO_FALLIMENTO = 'BAD_PASS' 
    and T.DT_FALLIMENTO > :tsLogin"/>
<module-option name="qryDisableUser" 
    value="begin DISATTIVA_UTENTE(:nmUser, :tsLogin); end;"/>
```

Il file nel complesso dovrebbe risultare così:
```xml
<security-domain name="ShibUserPassAuth" cache-type="default">
    <authentication>
        <login-module name="tagish" code="com.tagish.auth.DBLogin" 
                flag="required">
            <module-option name="userTable" value="USR_USER"/>
            <module-option name="userColumn" value="NM_USERID"/>
            <module-option name="passColumn" value="CD_PSW"/>
            <module-option name="saltColumn" value="CD_SALT"/>
            <module-option name="activeColumn" value="FL_ATTIVO"/>
            <module-option name="expirationColumn" 
                value="DT_SCAD_PSW"/>
            <module-option name="useJndiLookup" value="true"/>
            <module-option name="jndiName"
                value="java:jboss/datasources/SiamDs"/>
            <module-option name="logTable"
                value="SACER_LOG.LOG_LOGIN_FALLITO"/>
            <module-option name="logTableCols"  
                value="NM_APPLIC,NM_USERID,CD_IND_SERVER,
                TIPO_FALLIMENTO,CD_IND_IP_CLIENT,DS_FALLIMENTO,
                DT_FALLIMENTO,ID_LOGIN_FALLITO"/>
            <module-option name="logTableValues"
                value=":nmApplic,:nmUser,:cdIndServer,:tipoFallimento,
                :cdIndIpClient,:dsFallimento,:tsFallimento,
                SACER_LOG.SLOG_LOGIN_FALLITO.nextval"/>
            <module-option name="serverNameSystemProperty"
                value="jboss.node.name"/>
            <module-option name="qryParamUserName" value="nmUser"/>
            <module-option name="qryParamDate" value="tsLogin"/>
            <module-option name="qryRetrieveMaxDays"
                value="SELECT v.ds_valore_param_applic
                FROM IAM_PARAM_APPLIC p JOIN IAM_VALORE_PARAM_APPLIC v
                ON (p.id_param_applic=v.id_param_applic)
                AND p.NM_PARAM_APPLIC = 'MAX_GIORNILOGGER'
                AND v.TI_APPART = 'APPLIC'"/>
            <module-option name="qryRetrieveMaxTry"
                value="SELECT v.ds_valore_param_applic
                FROM IAM_PARAM_APPLIC p JOIN IAM_VALORE_PARAM_APPLIC v
                ON (p.id_param_applic=v.id_param_applic)
                AND p.NM_PARAM_APPLIC = 'MAX_TENTATIVI_FALLITI'
                AND v.TI_APPART = 'APPLIC'"/>
            <module-option name="qryRetrieveLastAccess"
                value="select T.DT_EVENTO 
                from SACER_LOG.LOG_LOGIN_USER t
                where T.NM_USERID = :nmUser
                and T.DT_EVENTO > :tsLogin
                and T.TIPO_EVENTO = 'LOGIN'
                order by T.DT_EVENTO desc"/>
            <module-option name="qryRetrieveFailedLogins"
                value="select count(*)
                from SACER_LOG.LOG_LOGIN_FALLITO t
                where T.NM_USERID = :nmUser
                and TIPO_FALLIMENTO = 'BAD_PASS'
                and T.DT_FALLIMENTO > :tsLogin"/>
            <module-option name="qryDisableUser"
                value="begin DISATTIVA_UTENTE(:nmUser, :tsLogin); end;"/>
        </login-module>
    </authentication>
</security-domain>
```

I parametri appena elencati dipendono dalla versione del modulo *idp-jaas-rdbms*, ad oggi l'ultima versione è la **0.0.11**.

## Configurazione del connettore jca XADisk

*Configuration > Subsystem > Resource Adapters*

**(+)**

Chiave | Valore 
------ | ------
Archive | XADisk.rar
Module | - 
Name | XADisk.rar

Poi cliccando su **View** si entra nel dettaglio ed è possibile definire:

*Configuration -> Attributes*

Chiave | Valore 
--- | ------
TX | XATransaction
Config Properties | instanceId=xaDisk1 synchronizeDirectoryChanges=true xaDiskHome=/opt/jboss-eap/parer_snap/xaDiskHome enableRemoteInvocations=false

**Connection Definitions**

Cliccare su **Add**

Chiave | Valore 
--- | ------
Name | xaDiskPool
Class name | org. xadisk. connector. outbound. XADiskManagedConnectionFactory

Quindi cliccando su **Edit** modificare 

Chiave | Valore 
--- | ------
Config Properties | instanceId=xaDisk1
JNDI Name | java:/jca/xadiskLocal

Infine nel tab **Pool** impostare:

Chiave | Valore 
--- | ------
Min Pool Size | 1
Max Pool Size | 5

Tornare nella pagina principale della console web e andare in **Deployments** e cliccare su **(+) > Upload Deployment**  e caricare 
il file **XADisk.rar**, name e Runtime Name devono essere **XADisk.rar**.  
Assicurarsi che il deployment sia "enabled and active".

Si può configurare allo stesso modo tramite CLI
```bash 
/subsystem=resource-adapters/resource-adapter=XADisk.rar:add(archive=XADisk.rar,transaction-support=XATransaction)
/subsystem=resource-adapters/resource-adapter=XADisk.rar/config-properties=instanceId:add(value=xaDisk1)
/subsystem=resource-adapters/resource-adapter=XADisk.rar/config-properties=synchronizeDirectoryChanges:add(value=true)
/subsystem=resource-adapters/resource-adapter=XADisk.rar/config-properties=xaDiskHome:add(value="/opt/jboss-eap/parer_snap/xaDiskHome")
/subsystem=resource-adapters/resource-adapter=XADisk.rar/config-properties=enableRemoteInvocations:add(value=false)
/subsystem=resource-adapters/resource-adapter=XADisk.rar/connection-definitions=xaDiskPool:add(class-name=org.xadisk.connector.outbound.XADiskManagedConnectionFactory,jndi-name=java:/jca/xadiskLocal,min-pool-size=1,max-pool-size=5)
/subsystem=resource-adapters/resource-adapter=XADisk.rar/connection-definitions=xaDiskPool/config-properties=instanceId:add(value=xaDisk1)
```

## Utilizzo di XADisk e NFS

Non è necessario montare le folder con il parametro "sync" perché XADisk forza il fushing delle folder commit
time tramite fsync.  
Nel caso delle cache di lookup è invece necessario impostare il parametro *lookupcache=positive*
per non mantenere la cache di entry non presenti sul server.  

## Configurazione dei webservices

Il subsystem webservices necessita della modifica di 3 parametri che regolano la riscrittura del tag
*soap:address* quando viene generato dinamicamente il wsdl.  
Nello specifico, vanno modificati come segue gli attributi **wsdl-port**, **wsdl-secure-port** 
e **wsdl-host**:  

```bash
/subsystem=webservices:write-attribute(name=modify-wsdl-address,value=true)
/subsystem=webservices:write-attribute(name=wsdl-port,value=80)
/subsystem=webservices:write-attribute(name=wsdl-secure-port,value=443)
/subsystem=webservices:write-attribute(name=wsdl-host,value=jbossws.undefined.host)
```

## Proprietà di sistema

Per il corretto funzionamento delle applicazioni è necessario aggiungere la seguente proprietà di sistema:

Chiave | Valore
--- | ---
org.apache.tomcat.util.http.Parameters.MAX_COUNT | 10.000
discovery-service-enabled | false
abilita-livello-1-spid | false
object-storage-enabled | true
xformer-sp-identity-id | https://${base-url}/xformer
user.country | IT
user.language | it
fed-metadata-url | https://${base-url}/saceriam/federationMetadata

```bash
/system-property=org.apache.tomcat.util.http.Parameters.MAX_COUNT:add(value=10000)
/system-property=discovery-service-enabled:add(value="false")
/system-property=abilita-livello-1-spid:add(value="false")
/system-property=object-storage-enabled:add(value="true")
/system-property=xformer-sp-identity-id:add(value="https://${base-url}/xformer")
/system-property=user.country:add(value="IT")
/system-property=user.language:add(value="it")
/system-property=fed-metadata-url:add(value="https://${base-url}/saceriam/federationMetadata")
```
## Logging profiles

Per standardizzare la scrittura dei log per le applicazioni rilasciate vengono utilizzati i logging-profile
forniti da JBoss.  
Le configurazioni possono essere applicate e modificate "a caldo" e dipendono dal nome del profilo
associato al server-group.  
Ogni applicazione dispone di un logging-profile specifico con stesso formato di tracciamento ma
diverse regole.  

Esempio di configurazione tramite CLI di un logging profile:  
```bash
batch

echo "Logging-Profile: XFORMER (Snap)"
/subsystem=logging/logging-profile=XFORMER:add

echo "Log su file"
/subsystem=logging/logging-profile=XFORMER
    /periodic-rotating-filehandler= xformer_handler
    :add(file={"path"=>"xformer.log",
    "relative-to"=> "jboss.server.log.dir"}, suffix=".yyyy-MM-dd"
    ,append="true" )

/subsystem=logging/logging-profile=XFORMER
    /periodic-rotating-filehandler=xformer_handler
    :write-attribute(name="level", value="DEBUG")
/subsystem=logging/logging-profile=XFORMER
    /periodic-rotating-filehandler=xformer_handler
    :write-attribute(name="formatter",
    value="%d{yyyy-MM-dd HH:mm:ss,SSS}%-5p [%c] (%t) %s%E%n")

echo "Logger specifici"
/subsystem=logging/logging-profile=XFORMER
    /logger=org.springframework:add
/subsystem=logging/logging-profile=XFORMER   
    /logger=org.springframework
    :write-attribute(name="use-parent-handlers", value="true")
/subsystem=logging/logging-profile=XFORMER
    /logger=org.springframework
    :write-attribute(name="level", value="ERROR")
/subsystem=logging/logging-profile=XFORMER
    /logger=org.opensaml:add
/subsystem=logging/logging-profile=XFORMER
    /logger=org.opensaml
    :write-attribute(name="use-parent-handlers", value="true")
/subsystem=logging/logging-profile=XFORMER
    /logger=org.opensaml:write-attribute(name="level", value="ERROR")
/subsystem=logging/logging-profile=XFORMER
    /logger=org.exolab.castor.xml.-NamespacesStack:add
/subsystem=logging/logging-profile=XFORMER
    /logger=org.exolab.castor.xml.-NamespacesStack
    :write-attribute(name="use-parent-handlers", value="true")
/subsystem=logging/logging-profile=XFORMER
    /logger=org.exolab.castor.xml.-NamespacesStack
    :write-attribute(name="level", value="OFF")
/subsystem=logging/logging-profile=XFORMER
    /logger=org.exolab.castor.xml.EndElementProcessor:add
/subsystem=logging/logging-profile=XFORMER
    /logger=org.exolab.castor.xml.EndElementProcessor
    :write-attribute(name="use-parent-handlers", value="true")
/subsystem=logging/logging-profile=XFORMER
    /logger=org.exolab.castor.xml.EndElementProcessor
    :write-attribute(name="level", value="ERROR")
/subsystem=logging/logging-profile=XFORMER
    /logger=it.eng.xformer:add
/subsystem=logging/logging-profile=XFORMER
    /logger=it.eng.xformer
    :write-attribute(name="use-parent-handlers", value="true")
/subsystem=logging/logging-profile=XFORMER
    /logger=it.eng.xformer
    :write-attribute(name="level", value="DEBUG")
/subsystem=logging/logging-profile=XFORMER
    /rootlogger=ROOT:add(handlers=[xformer_handler])
/subsystem=logging/logging-profile=XFORMER
    /root-logger=ROOT:write-attribute(name ="level",value="INFO")

run-batch
```

Di seguito il contenuto XML prodotto dalla configurazione precedente:

```xml
<logging-profile name="XFORMER">
    <periodic-rotating-file-handler name="xformer_handler">
        <level name="DEBUG"/>
        <formatter>
            <pattern-formatter 
                pattern="%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%E%n"/>
        </formatter>
        <file relative-to="jboss.server.log.dir" path="xformer.log"/>
        <suffix value=".yyyy-MM-dd"/>
        <append value="true"/>
    </periodic-rotating-file-handler>
    <logger category="org.springframework" use-parent-handlers="true">
        <level name="ERROR"/>
    </logger>
    <logger category="org.opensaml" use-parent-handlers="true">
        <level name="ERROR"/>
    </logger>
    <logger category="org.exolab.castor.xml.NamespacesStack"
        use-parent-handlers="true">
        <level name="OFF"/>
    </logger>
    <logger category="org.exolab.castor.xml.EndElementProcessor"
        use-parent-handlers="true">
        <level name="ERROR"/>
    </logger>
    <logger category="it.eng.xformer" use-parent-handlers="true">
        <level name="DEBUG"/>
    </logger>
    <root-logger>
        <level name="INFO"/>
        <handlers>
            <handler name="xformer_handler"/>
        </handlers>
    </root-logger>
</logging-profile>
```

### Custom JDBC handler

Per consentire una migliore gestione dei log delle query fatte tramite Hibernate è stato implementato il modulo custom **jboss-module-application-logger**. 

Per installare il modulo bisogna scaricare il progetto 

https://gitlab.ente.regione.emr.it/parer/tools/jboss-module-application-logger

e lanciare un 

```bash 
mvn clean compile
```
si ottiene un file *jboss-application-logger-x.y.z.zip* che va scompattato nella cartella *${JBOSS_HOME}/module*.

Vanno infine configurati due logger a cui le singole applicazioni andranno ad aggiungere i propri handler

```xml
<subsystem xmlns="urn:jboss:domain:logging:1.5">
    <!-- ... -->
    <logger category="jboss.jdbc.spy" use-parent-handlers="false">
        <level name="DEBUG"/>
        <filter-spec value="match(&quot;Statement|prepareStatement&quot;)"/>
    </logger>
    <logger category="org.hibernate" use-parent-handlers="false">
        <level name="INFO"/>
    </logger>
    <!-- ... -->
</subsystem>
```

```bash
/subsystem=logging/logger=org.hibernate:add(use-parent-handlers=false,level=INFO)

/subsystem=logging/logger=jboss.jdbc.spy:add(use-parent-handlers=false,level=DEBUG)

/subsystem=logging/logger=jboss.jdbc.spy:write-attribute(name=filter-spec,value=match("Statement|prepareStatement"))
```

### Modificare i livelli di logging 

Per modificare i livelli di logging andare, dalla console di amministrazione, su  
*Configuration > Core > Logging*.

Nel riquadro *handler* è possibile aggiungere console o file handler specifici.  
Nel riquadro *log categories* è possibile associare categorie specifiche di log da assegnare agli
handler.  


## Configurazione del listener HTTPS

Vengono utilizzati i subsystem elytron e undertow e in RER la connessione è possibile solo utilizzando un certificato client.
Inoltre, per questioni di sicurezza, si utilizza solo TLSv1.2

Tramite CLI questa configurazione si ottiene come di seguito 

```bash
/subsystem=elytron/key-store=RERcerts_KS:add(type=JKS,credential-reference={clear-text=xxx},path=/etc/pki/RER/jboss_cert_auth.jks)
/subsystem=elytron/key-manager=RERcerts_KM:add(key-store=RERcerts_KS,credential-reference={clear-text=xxx},algorithm=SunX509)
/subsystem=elytron/trust-manager=RERcerts_TM:add(key-store=RERcerts_KS)
/subsystem=elytron/key-store=wildcard_ente_regione_emr_it_KS:add(path=/etc/pki/RER/ente.regione.emr.it.jks,type=JKS,credential-reference={clear-text=xxx})
/subsystem=elytron/key-manager=wildcard_ente_regione_emr_it_KM:add(key-store=wildcard_ente_regione_emr_it_KS,algorithm=SunX509,credential-reference={clear-text=xxx})
/subsystem=elytron/server-ssl-context=wildcard_ente_regione_emr_it_SSC:add(key-manager=wildcard_ente_regione_emr_it_KM,protocols=["TLSv1.2"])
/subsystem=elytron/server-ssl-context=wildcard_ente_regione_emr_it_SSC:write-attribute(name=trust-manager,value=RERcerts_TM)
/subsystem=elytron/server-ssl-context=wildcard_ente_regione_emr_it_SSC:write-attribute(name=need-client-auth,value=true)
batch 
/subsystem=undertow/server=default-server/https-listener=https:undefine-attribute(name=security-realm)
/subsystem=undertow/server=default-server/https-listener=https:write-attribute(name=ssl-context,value=wildcard_ente_regione_emr_it_SSC)
run-batch
```

Per creare il jks con password (/etc/pki/RER/jboss_cert_auth.jks), come utente root:

```bash
 openssl x509 -outform der -in /etc/pki/RER/reram.pem -out /etc/pki/RER/reram.der
openssl x509 -outform der -in /etc/pki/RER/lblrer.pem -out /etc/pki/RER/lblrer.der
/usr/lib/jvm/java-1.8.0-openjdk/bin/keytool -import -alias lbl_ca -keystore /etc/pki/RER/jboss_cert_auth.jks -file /etc/pki/RER/lblrer.der
/usr/lib/jvm/java-1.8.0-openjdk/bin/keytool -import -alias reram -keystore /etc/pki/RER/jboss_cert_auth.jks -file /etc/pki/RER/reram.der 
chown root:rercerts /etc/pki/RER/jboss_cert_auth.jks
usermod -a -G rercerts jboss      
```
## Validazione del datasource

La configurazione di base dei datasource in RER viene fatta con il seguente comando CLI:

```bash
data-source --profile=PROFILE add --name=DS_NAME --jndi-name=DS_JNDI 
    --connection-url=DS_URL --user-name=DS_USER --password=DS_PASSWD 
    --driver-name=ojdbc11 
    --exception-sorter-classname
        =org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter 
    --stale-connection-checker-class-name
        =org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker
    --statistics-enabled=true --use-ccm=true --use-fast-fail=true
    --valid-connection-checker-class-name
        =org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker
    --validate-on-match=true --flush-strategy=FailingConnectionOnly
    --backgroundvalidation=false --enabled=true
```

Il tema è trattato al paragrafo 12.3.2 della [Configuration guide](#biblio), in particolare nella sezione "Datasource Parameters". 

## URI encoding 

Bisogna impostare come default UTF-8 per l'encoding degli URI.

```bash
/subsystem=undertow/servlet-container=default:write-attribute(name="default-encoding", value="utf-8")
```


## Configurazione security header

Con particolare attenzione ad aspetti legati alla sicurezza, si elenanco gli headers utilizzati come default e relativi valori: 

|  Header | System property | Default | Nota  |
|---|---|---|---|
|  [Content-Security-Policy](https://www.w3.org/TR/CSP1/)|  http.sec.header.content-security-policy | default-src ''self''; script-src ''self'' ''unsafe-inline'' ''unsafe-eval'' ''report-sample'' *; style-src ''self'' ''report-sample'' ''unsafe-inline'' *; img-src ''self'' data: *;  | vedere esempio sotto |
|  [X-Content-Type-Options](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Content-Type-Options) |  - | nosniff |  |
|  [Referrer-Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Referrer-Policy) |  - | strict-origin-when-cross-origin  |  |
|  [Permissions-Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/Permissions_Policy) |  http.sec.header.permissions-policy |  ccross-origin-isolated=\*,vertical-scroll=\* | vedere esempio sotto |

**Nota:** alcuni dei valori sono impostabili attraverso opportuna system property, se definite su application server, andranno a sostituire il valore di default impostato, come da esempio:

```bash
# Content-Security-Policy
/system-property=http.sec.header.content-security-policy:add(value="default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval' 'report-sample' *.regione.emilia-romagna.it *.ente.regione.emr.it; style-src 'self' 'report-sample' 'unsafe-inline' *.regione.emilia-romagna.it *.ente.regione.emr.it; img-src 'self' data: *.regione.emilia-romagna.it *.ente.regione.emr.it")

# Permissions-Policy
/system-property=http.sec.header.permissions-policy:add(value="accelerometer=*, ambient-light-sensor=*, autoplay=*, battery=*, camera=*, cross-origin-isolated=*, display-capture=*, document-domain=*, encrypted-media=*, execution-while-not-rendered=*, execution-while-out-of-viewport=*, fullscreen=*, geolocation=*, gyroscope=*, keyboard-map=*, magnetometer=*, microphone=*, midi=*, navigation-override=*, payment=*, picture-in-picture=*, publickey-credentials-get=*, screen-wake-lock=*, sync-xhr=*, usb=*, web-share=*, xr-spatial-tracking=*, clipboard-read=*, clipboard-write=*, gamepad=*, speaker-selection=*, conversion-measurement=*, focus-without-user-activation=*, hid=*, idle-detection=*, interest-cohort=*, serial=*, sync-script=*, trust-token-redemption=*, window-placement=*, vertical-scroll=*")

```
nell'esempio sopra verranno quindi sostituiti ai default, i valori indicati.

## Parametri di sistema e della JVM

I parametri di base utilizzati in RER per l'ambiente di test sono i seguenti:

```bash
JAVA_HOME=/usr/lib/jvm/jre-11-openjdk
JBOSS_HOME=/var/lib/jboss-eap
JBOSS_MODULEPATH=$JBOSS_HOME/modules:/opt/jboss-eap/modules
PUB_ADDR=<indirizzo_pubblico>
MGMT_ADDR=127.0.0.1
MULTICAST_ADDR=230.2.1.1
MESSAGING_PASS=<jms pass>
OFFSET=1
FLAVOUR=standalone-full-ha.xml
JAVA_OPTS="\
-server \
-Xms8192m \
-Xmx8192m \
-Djava.net.preferIPv4Stack=true \
-Djboss.modules.system.pkgs=org.jboss.byteman \
-Djava.awt.headless=true \
-Djavax.net.ssl.trustStore=/etc/pki/java/cacerts \
-XX:+DoEscapeAnalysis \
-XX:+UseCompressedOops \
-XX:+CMSClassUnloadingEnabled \
-XX:+ExplicitGCInvokesConcurrent \
-XX:CMSInitiatingOccupancyFraction=80 \
-XX:CMSIncrementalSafetyFactor=20 \
-XX:+UseCMSInitiatingOccupancyOnly \
-verbose:gc \
-Xlog:class+unload=off \
-Xlog:gc*:/opt/jboss-eap/parer_test/log/gc.log \
-Duser.country=IT \
-Duser.language=it \
-agentlib:jdwp=transport=dt_socket,address=*:12201,server=y,suspend=n \
"
```

## Configurazione customizzazioni interfaccia / altro

Le applicazioni PARER si presentano prive di loghi e/o elementi caratteristici che ne definiscono una "brandizzazione", per tale motivo sono state inserite le opportune configurazioni e dinamiche configurative che permettono una gestione esterna di alcune risorse (e di altre possibili configurazioni), che attraverso le system properties di JBoss vengono utilizzate dai singoli cotensti appliativi.
Di seguito si riportano le possibili configurazioni supportate.


| Nome parametro| Descrizione| Valore predefinito |
|------------------------|--------------------------------------------------------------------|--------------------|
| *disableSecurity*      | Permette di disabilitare i controlli di sicurezza tra le varie pagine. | **false**   |
| *enableLazySort*       | Abilita la possibilità di ordinare le colonne delle liste.        | **true**         |
| *debugAuthorization*   | Debug sulle informazioni relative all'autorizzazione degli utenti.| **false**        |
| *logoApp_absolutePath* | Percorso assoluto del logo dell'applicazione. Se configurato sovrascrive l'utilizzo del percorso relativo. |  |
| *logoApp_relativePath* | Percorso relativo del logo dell'applicazione.   | **/img/logos/logo_app.png** |
| *logoApp_alt*          | Nome alternativo del logo dell'applicazione (se non viene trovata l'immagine).     |    |
| *logoApp_url*          | Pagina web associata al logo dell'applicazione.   |    |
| *logoApp_title*        | Title per il logo dell'applicazione. |   |
| *logo1_absolutePath*   | Percorso assoluto del "logo 1". Se configurato sovrascrive l'utilizzo del percorso relativo. |  |
| *logo1_relativePath*   | Percorso relativo del "logo 1".     | **/img/logos/logo1.png** |
| *logo1_alt*            | Nome alternativo del "logo 1" (se non viene trovata l'immagine).     |    |
| *logo1_url*            | Pagina web associata al "logo 1".   |    |
| *logo1_title*          | Title per il "logo 1". |    |
| *logo2_absolutePath*   | Percorso assoluto del "logo 2". Se configurato sovrascrive l'utilizzo del percorso relativo. |  |
| *logo2_relativePath*   | Percorso relativo del "logo 2".   | **/img/logs/logo2.png**   |
| *logo2_alt*            | Nome alternativo del "logo 2" (se non viene trovata l'immagine).  |   |
| *logo2_url*            | Pagina web associata al "logo 2".   |   |
| *logo2_title*          | Title per il "logo 2".            |    |
| *logo3_absolutePath*   | Percorso assoluto del "logo 3". Se configurato sovrascrive l'utilizzo del percorso relativo. |   |
| *logo3_relativePath*   | Percorso relativo del "logo 3".   | **/img/logos/logo3.png**    |
| *logo3_alt*            | Nome alternativo del "logo 3" (se non viene trovata l'immagine).   |  |
| *logo3_url*            | Pagina web associata al "logo 3".            |    |
| *logo3_title*          | Title per il "logo 3".  |    |
| *favicon_absolutePath* | Percorso assoluto della favicon. Se configurato sovrascrive l'utilizzo del percorso relativo.    |   |
| *favicon_relativePath* | Percorso relativo della favicon.  | **/img/logos/favicon.ico**   |
| *cssover_absolutePath* | Percorso assoluto dell'overlay sui css. Se configurato sovrascrive l'utilizzo del percorso relativo.  |   |
| *cssover_relativePath* | Percorso relativo dell'overlay sui css.    | **/css/slForms-over.css**  |
| *titolo_applicativo*   | Titolo (title html) dell'applicazione.           | **Titolo Applicazione** |
| *ambiente_deploy*      | Ambiente su cui viene effettuato il deploy dell'applicazione  |    |
| *enableHelpOnline*     | Abilita l'help online.  | **false**  |


Oltre alle risorse statiche con le quali è possibile, per ambiente, customizzare l'aspetto grafico delle singoli applicazioni, vengono introdotti alcuni parametri che riguardano le configurazioni legate ad alcuni WS Rest esposti dalle stesse, attraverso tali parametri è quindi possibile "personalizzare" alcuni valori da essi utilizzati.
Si riporta nella seguente tabelle di quali parametri è possibile modificare il valore (con "null" si intende nessun valore impostato di default):

| Nome parametro| Descrizione| Valore predefinito |
|------------------------|--------------------------------------------------------------------|--------------------|
| *ws.instanceName*     | Nome istanza.  | **minefield**  |
| *ws.upload.directory*     | Directory su cui si effettua l'upload dei file raw presenti sulla chiamata multipart/form-data.  | **/tmp**  |
| *versamentoSync.saveLogSession*     | Versamento sicrono unità documentaria: abilitazione dei log di sessione.  | **true**  |
| *versamentoSync.maxRequestSize*     |  Versamento sicrono unità documentaria: dimensione massima della richiesta multipart/form-data in byte. | **1000000000** (1 Gbyte) |
| *versamentoSync.maxFileSize*     |  Versamento sicrono unità documentaria: dimensione massima del singolo file presente su richiesta multipart/form-data in byte.  | **1000000000** (1 Gbyte) |
| *aggAllegati.saveLogSession*     |  Aggiunta documento: abilitazione dei log di sessione.  | **false**  |
| *aggAllegati.maxRequestSize*     |   Aggiunta documento: dimensione massima della richiesta multipart/form-data in byte. | **1000000000** (1 Gbyte) |
| *aggAllegati.maxFileSize*     |   Aggiunta documento: dimensione massima del singolo file presente su richiesta multipart/form-data in byte.  | **1000000000** (1 Gbyte) |
|  *profilerApp.upload.directory*     |  Applicazione profile: directory su cui si effettua l'upload dei file raw presenti sulla chiamata multipart/form-data.  | **/tmp**  |
|  *profilerApp.maxRequestSize*     | Applicazione profile:dimensione massima della richiesta multipart/form-data in byte.  | **1000000000**  (1 Gbyte) |
|  *profilerApp.maxFileSize*     | Applicazione profile: dimensione massima del singolo file presente su richiesta multipart/form-data in byte.| **10000000** (10 Mbyte)  |
|  *profilerApp.charset*     |  Applicazione profile: charset di riferimento. | **UTF-8**  |
| *recuperoSync.saveLogSession*    | Servizi di recupero:  abilitazione dei log di sessione.  | **false**  |
| *recuperoSync.maxResponseSize*    | Servizi di recupero:  dimensione massima della risposta restituida dal servizio in byte.   | **20000000** (20 Mbyte) |
| *recuperoSync.maxFileSize*     |  Servizi di recupero: dimensione massima del singolo file presente su richiesta multipart/form-data in byte. | **20000000** (20 Mbyte) |
| *loadXsdApp.upload.directory*     |  Caricamento modello XSD:  directory su cui si effettua l'upload dei file.   | **/tmp**  |
| *loadXsdApp.maxRequestSize*     |  Caricamento modello XSD:  dimensiome massima della richiesta.  | **1000000000**  (1 Gbyte) |
| *loadXsdApp.maxFileSize*     | Caricamento modello XSD:  dimensiome massima del file xsd caricato.   | **10000000** (1 Mbyte)  |
| *loadXsdApp.charset*    | Caricamento modello XSD: charset di riferimento. | **UTF-8**  |
| *moduloInformazioni.upload.maxFileSize*    | Modulo Informazioni: dimensiome massima del file caricato. | **10000000** (1 Mbyte) |
| *helpOnline.upload.maxFileSize*    | Help Online: dimensiome massima del file caricato.  | **10000000** (1 Mbyte)  |
| *variazioneAccordo.upload.maxFileSize*    |  Variazione Accordo: dimensiome massima del file caricato. | **10000000** (1 Mbyte) |
| *disciplinareTecnico.upload.maxFileSize*    |  Disciplinare Tecnico: dimensiome massima del file caricato. | **10000000** (1 Mbyte) |
| *importVersatore.upload.maxFileSize*    |  Import Versatore: dimensiome massima del file caricato. | **10000000** (1 Mbyte) |
| *parameters.csv.maxFileSize*    |  Parametri CSV: dimensiome massima del file caricato. | **10000000** (1 Mbyte) |
| *serverName.property*    |  Property di sistema per identificare il nome del server/nodo (e.s. su Jboss identificato con il valore jboss.node.name). | jboss.node.name |
| *uri.versamentoSync*    |  URI API versamento sincrono unità documentaria. |  |


Nota: alcune delle property sopra elencate sono il frutto di un censimento "globale" che non trovo però sempre applicazione, alcune di essere sono infatti da ritenersi non più utilizzate (vedi ~~non utilizzata~~).

La logica con cui vengono letti e configurati i parametri d'inizializzazione è la seguente:

1. il parametro è presente sul web.xml;
2. è presente un parametro dal nome ```applicazione-nome_parametro``` sulle proprietà di sistema;
3. è presente un parametro dal nome ```nome_parametro``` sulle proprietà di sistema;
4. viene utilizzato un valore predefinito per il parametro.

Nel caso in cui si ricada in uno dei casi sopracitati, tutti gli altri non vengono valutati.

Le risorse con un percorso assoluto non vengono referenziate sull'HTML delle pagine come risorse esterne ma vengono **incluse** nell'applicazione  a **deploy time**.
In particolare:

 * il logo dell'applicazione (*logoApp_absolutePath*) sarà disponibile all'applicazione al percorso `/img/external/logo_app.png`
 * il logo in alto a destra (*logo1_absolutePath*) sarà disponibile all'applicazione al percorso `/img/external/logo1.png`
 * il logo in basso a sinistra (*logo2_absolutePath*) sarà disponibile all'applicazione al percorso `/img/external/logo2.png`
 * il  logo in basso a destra (*logo3_absolutePath*) sarà disponibile all'applicazione al percorso `/img/external/logo3.png`
 * la favicon (*favicon_absolutePath*) sarà disponibile all'applicazione al percorso `/img/external/favicon.ico`
 * la sovrascrittura stili CSS (*cssover_absolutePath*) sarà disponibile all'applicazione al percorso `/css/external/slForms-over.css`

Tali risorse devono essere espresse come URL e possono referenziare indirizzi remoti oppure file. Ecco un esempio:
 * logo1_absolutePath: `https://parer.pages.ente.regione.emr.it/risorse-statiche/parer-svil/sacer/img/logo_sacer_small.png` 
 * logo1_absolutePath: `file:///opt/jbosseap/images/logo_sacer_small.png`

Siccome le risorse vengono caricate all'interno dell'applicazione <ins>non è necessario che siano esposte esternamente</ins>. Questo significa che anche per ambienti esposti su internet i loghi possono essere registrati su percorsi interni alle rete dell'application server.

Disposizione dei loghi all'interno della pagina:

```
,--------------------------------------.                           
|logo applicazione |          | logo 1 |
|------------------'          '--------|
|                                      |
|                                      |
|                                      |
|                                      |
|-------,                     ,--------|
|logo 2 |                     | logo 3 |  
`--------------------------------------' 
```
Infine un esempio di script cli con cui inserire le system properties sopra citate:

```bash
# Common
/system-property=logoApp_title:add(value="Applicazione - Polo Archivistico Regionale dell'Emilia-Romagna")
/system-property=logoApp_alt:add(value="Polo Archivistico Regionale dell'Emilia-Romagna")
/system-property=logo1_title:add(value="ParER - Polo Archivistico Regionale dell'Emilia-Romagna")
/system-property=logo1_alt:add(value="Logo")
/system-property=logo1_url:add(value="https://poloarchivistico.regione.emilia-romagna.it/")
/system-property=logo1_absolutePath:add(value="file:///<path>/LogoParer.png")

/system-property=logo2_title:add(value="Regione Emilia-Romagna")
/system-property=logo2_alt:add(value="Regione Emilia-Romagna")
/system-property=logo2_url:add(value="https://www.regione.emilia-romagna.it")
/system-property=logo2_absolutePath:add(value="file:///<path>/LogoER.png")

/system-property=logo3_absolutePath:add(value="file:///<path>/LogoIbc.png")

/system-property=favicon_absolutePath:add(value="file:///<path>/favicon.ico")


# Per applicazione

/system-property=sacer-titolo_applicativo:add(value="SACER")
/system-property=sacer-logoApp_alt:add(value="SACER")
/system-property=sacer-logo1_alt:add(value="SACER")
/system-property=sacer-logoApp_absolutePath:add(value="file:///<path>/logo_sacer_small.png")
/system-property=sacer-cssover_absolutePath:add(value="file:///<path>/slForms-over.css")


/system-property=sacerping-titolo_applicativo:add(value="PING")
/system-property=sacerping-logoApp_alt:add(value="PING")
/system-property=sacerping-logo1_alt:add(value="PING")
/system-property=sacerping-logoApp_absolutePath:add(value="file:///<path>/logo_sacer_small.png")
/system-property=sacerping-cssover_absolutePath:add(value="file:///<path>/slForms-over.css")


/system-property=saceriam-titolo_applicativo:add(value="SIAM")
/system-property=saceriam-logoApp_alt:add(value="SIAM")
/system-property=saceriam-logo1_alt:add(value="SIAM")
/system-property=saceriam-logoApp_absolutePath:add(value="file:///<path>/logo_sacer_small.png")
/system-property=saceriam-cssover_absolutePath:add(value="file:///<path>/slForms-over.css")


/system-property=verso-titolo_applicativo:add(value="VERSO")
/system-property=verso-logoApp_alt:add(value="VERSO")
/system-property=verso-logo1_alt:add(value="VERSO")
/system-property=verso-logoApp_absolutePath:add(value="file:///<path>/Logo-SACER-VERSO_Beta.png")
/system-property=verso-uri.versamentoSync:add(value="https://parer.regione.emilia-romagna.it/sacerws/VersamentoSync")


/system-property=sacerdips-titolo_applicativo:add(value="DIPS")
/system-property=sacerdips-logoApp_alt:add(value="DIPS")
/system-property=sacerdips-logo1_alt:add(value="DIPS")
/system-property=sacerdips-logoApp_absolutePath:add(value="file:///<path>/logo_sacer_small.png")
/system-property=sacerdips-cssover_absolutePath:add(value="file:///<path>/slForms-over.css")

```

## Versioni di riferimento

Qui di seguito una tabella riassuntiva delle versioni dei componenti utilizzati per la configurazione di
JBoss EAP.

Nome | Versione | Nativo JBoss EAP 7.4 | Descrizione
--- | --- | --- | ---
JBoss EAP |     7.4.7.GA |   |Versione completa dell'EAP
Hibernate | 5.4.x | sì | Motore ORM utilizzato come modulo JBoss
Modulo ActiveMQ Artemis | 2.9.x | sì | Implementazione code JMS
XADisk | 1.2.2.5 | no | Transazioni XA su filesystem
Oracle Jdbc | 11 per Oracle 19c | no |Driver per accedere al DB Oracle
idp-jaasrdbms | 0.0.6 | no |Modulo utilizzato per la personalizzazione delle azioni associate al login

\newpage

## Riferimenti

**[Configuration guide]** - https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/html-single/configuration_guide/index

[Configuration guide]: https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/html-single/configuration_guide/index

**[Patching and Upgrading guide]** - https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/html-single/patching_and_upgrading_guide/index

[Patching and Upgrading guide]: https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/html-single/patching_and_upgrading_guide/index

**[Documentazione RedHat]** - https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/

[Documentazione RedHat]: https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/
