---
title: "Configurazione di JBoss EAP 6.4"
---

# Configurazione Application Server JBoss 6.4 EAP

## Versioni

| Versione | Modifiche | Data
| -------- | ---------- | ----------
1.0 | Prima emissione | 23/02/2017
1.1 | Correzioni ed aggiunte da parte di Lorenzo Dalrio | 05/04/2017
1.2 | Aggiunta configurazione timer su db | 26/04/2017
1.3 | Aggiunta sezione per disattivazione Load Balancing | 19/05/2017
1.4 | Aggiunte informazioni sui logging profile e cancellazione della parte relativa al datasource per i timer | 01/09/2017
1.5 | Aggiunte sezioni per dettagliare la configurazione delle code JMS e altre configurazioni per gli ambienti server | 01/09/2017
1.6 | Rese generiche sezioni con configurazioni specifiche per ambienti | 06/09/2017
1.7 | Versione finale, disattivata la registrazione delle modifiche | 06/07/2017
1.8 | Aggiunta sezione sulle configurazioni del subsystem webservice | 12/09/2017
1.9 | Aggiunta nuova proprietà di sistema ed aggiornamento delle configurazioni del modulo dell’idp | 25/09/2017
2.0 | Modificati riferimenti a nexus, corretto refuso su SacerPool | 
2.1 | Inserite informazioni per installazioni senza IDP ParER | 08/08/2018
2.2 | Inserite configurazioni per code JMS utilizzate da  Sacer (MEV#15295) | 06/09/2018
2.3 | Modificata numerazione paragrafi; aggiunti chiarimenti per configurazioni code JMS | 24/09/2018
2.4 | Aggiunte [regole di rewrite](#regole-di-rewrite) per SacerWS 24 | 02/10/2018
2.5 | Aggiunte configurazioni JMS-Bridge per micro-servizi ed aggiornamento eclispelink integration. Necessario modulo Artemis | 08/10/2018
2.6 | Inserite configurazioni per i bean pool dei due consumer per la gestione della migrazione su ObjectStorage | 12/11/2018
2.7 | Rivisti i limiti dei pool per la coda indice aip ([Bean pool per gli MDB](#bean-pool-per-gli-mdb)); Aggiunte le versioni ([Versioni di riferimento](#versioni-di-riferimento)) dei componenti di Jboss; Aggiunta configurazione collegamento SSL al bridge JMS([Configurazione JMS per migrazione blob su object storage](#Configurazione-JMS-per-migrazione-blob-su-object-storage)) | 29/11/2018
2.8 | Aggiunta proprietà di sistema per censire i jks ed altre proprietà collegate alla gestione della federazione( [Proprietà di sistema](#proprietà-di-sistema)) | 07/02/2019
2.9 | Modifica valori security domain associato alla release di saceriam 3.13.0 ([Configurazione del security domain](#Configurazione-del-security-domain)) | 07/05/2019
2.10 | Modificato per maggiore chiarezza il paragrafo [Configurazione JMS per migrazione blob su object storage](#Configurazione-JMS-per-migrazione-blob-su-object-storage); inserite le  proprietà fed-metadata-url e Idp-identity-id tra le proprietà di sistema (omesse per errore) | 19/09/2019
2.11 | Aggiunta nuovo data-source per il versamenti falliti [Configurazione datasource non XA](#Configurazione-datasource-non-XA). Aggiunta nuove proprietà di sistema per le credenziali aws di Xformer (reportistica) e Ping (strumenti urbanistici) [Proprietà di sistema](#proprietà-di-sistema).| 08/10/2019
3.0 | Passaggio alla versione markdown con documentazione separata per aspetti specifici delle singole applicazioni.| 02/04/2021

Il presente documento definisce le configurazioni da apportare all'application server JBoss EAP 6.4 necessarie alla corretta esecuzione degli applicativi sviluppati da ParER (Polo Archivistico Regione Emilia-Romagna).  
JBoss è configurabile in modalità "standalone" oppure "domain" a seconda che sia, rispettivamente, eseguito in un singolo nodo oppure  in un cluster di più nodi. In ParER gli ambienti di snapshot, test, pre-produzione e produzione sono configurati in modalità "domain" (vedi 5).  
In questo documento la cartella radice di JBoss verrà indicata spesso con ${JBOSS_HOME}. Tutti i percorsi su filesystem (a meno che non sia indicato esplicitamente) sono relativi a questa cartella.

## JDK

La JDK di riferimento utilizzata dalle applicazioni descritte in questo documento è la Open JDK versione 8.  
Se dovesse rivelarsi necessario utilizzare la versione di Java fornita da Oracle è necessario assicurarsi che siano correttamente installate le Java Cryptography Extension (JCE) Unlimited Strength.  
In ogni caso consigliamo fortemente di utilizzare la OpenJDK.

## Aggiunta amministratore application server

Per poter effettuare operazioni amministrative all'interno dell'installazione di JBoss può essere utile creare almeno un utente afferente al ruolo **ManagementRealm**. In ambiente standalone tale operazione può essere effettuata tramite lo script **bin/add-user.sh** (o .bat per windows).
Si può in alternativa decidere di installare e getire direttamente con l'utente root di S.O.

Ecco un esempio dell'esecuzione dello script in cui l'utenza inserita è **admin.jboss.eap** e password **secretpassword**.  

```bash
$ ./opt/jboss-eap-6.4.0/bin/add-user.sh

What type of user do you wish to add?
 a) Management User (mgmt-users.properties)
 b) Application User (application-users.properties)
(a):

Enter the details of the new user to add.
Using realm 'ManagementRealm' as discovered from the existing property files.
Username : admin.jboss.eap
Password requirements are listed below. To modify these restrictions edit the add-user.properties configuration file.
 - The password must not be one of the following restricted values {root, admin, administrator}
 - The password must contain at least 8 characters, 1 alphabetic character(s), 1 digit(s), 1 non-alphanumeric symbol(s)
 - The password must be different from the username
Password :
Re-enter Password :
What groups do you want this user to belong to? (Please enter a comma separated list, or leave blank for none)[  ]:
About to add user 'admin.jboss.eap' for realm 'ManagementRealm'
Is this correct yes/no? yes
Added user 'admin.jboss.eap' to file '/opt/jboss-eap-6.4.0/standalone/configuration/mgmt-users.properties'
Added user 'admin.jboss.eap' to file '/opt/jboss-eap-6.4.0/domain/configuration/mgmt-users.properties'
Added user 'admin.jboss.eap' with groups  to file '/opt/jboss-eap-6.4.0/standalone/configuration/mgmt-groups.properties'
Added user 'admin.jboss.eap' with groups  to file '/opt/jboss-eap-6.4.0/domain/configuration/mgmt-groups.properties'
Is this new user going to be used for one AS process to connect to another AS process?
e.g. for a slave host controller connecting to the master or for a Remoting connection for server to server EJB calls.
yes/no? y
To represent the user add the following to the server-identities definition <secret value="c2VjcmV0cGFzc3dvcmQ=" />
```

## Configurazione Standalone

La versione "standalone" di JBoss si declina in 4 modalità a seconda dei moduli precaricati:  

 - **standalone.xml**: supporta Java EE Web profile più alcune estensioni come servizi web RESTFul e invocazioni remote a EJB3. É il profilo predefinito;  
 - **standalone-full.xml**: Supporta Java EE Full-Profile e tutte le funzionalità lato server senza il clustering. Questo profilo supporta anche le code JMS;  
 - **standalone-ha.xml**: profilo predefinito + clustering;  
 - **standalone-full-ha.xml**: Java EE Full-Profile + clustering.  

Il modello di riferimento per l'ambiente di sviluppo locale è **full**. Se l'ambiente non necessita dell'uso delle code è sufficiente il modello predefinito.  
Per impostare la configurazione full bisogna sostituire il file `standalone/configuration/standalone.xml` con il contenuto del file `standalone/configuration/standalone-full.xml`.  

## Configurazione domain

In Parer, gli ambienti server di snapshot, test, pre-produzione e produzione **sono stati configurati in modalità domain**. Il profilo usato per tutti gli ambienti server è **full-ha**.  
La modalità domain permette la gestione centralizzata della configurazione, dei deploy e delle operazioni di start e stop dei server JBoss.  
Maggiori dettagli sulla modalità domain possono essere trovati al seguenti link.

https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/About_Managed_Domains.html

Dove non espressamente indicato tutte le configurazioni presenti in questo documento possono essere applicate indipendentemente dall'utilizzo in domain o standalone di JBoss.

Per poter effettuare i deploy in modalità rollling update si è deciso di dividere il cluster in due (parer-prod-A e parer-prod-B). Se la modalità rollling update non è di interesse, la configurazione dei due cluster può anche non essere attuata.

## Configurazione JDBC

### Driver Oracle

Il DBMS di riferimento è oracle alla versione 19c e la versioni del driver Oracle JDBC è la 8, l'installazione è spiegata nella [Administration_and_Configuration_Guide](https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Example_datasources.html#Example_Oracle_datasource).

Creare un nuovo modulo nella cartella  

`${JBOSS_HOME}/modules/system/layers/base/com/oracle/**ojdbc8**/main`

e copiarci il file **ojdbc8.jar**. Il nome in grassetto è convenzionale.  
Nella cartella, inoltre, bisogna creare il file **module.xml** con il seguente contenuto:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.1" name="com.oracle.ojdbc8">
	<resources>
		<resource-root path="ojdbc8.jar"/>
	</resources>
	<dependencies>
		<module name="javax.api"/>
		<module name="javax.transaction.api"/>
	</dependencies>
</module>
```
### Driver Eclipselink

Gli applicativi del ParER al momento dipendono da Eclipselink come ORM. Siccome il provider ORM di JBoss EAP è hibernate è necessario aggiungere all'application server il modulo di Eclipselink ed un modulo di integrazione (per esempio per la funzionalità di autodiscovery delle classi).  
La creazione dei moduli è semplificata dal progetto disponibile all'url seguente:  
https://github.com/lsnidero/as7-eclipselink-integration  
Procedura di compilazione ed installazione:  

```bash
$ git clone https://github.com/lsnidero/as7-eclipselink-integration.git
$ cd as7-eclipselink-integration/
$ mvn -Declipselink.version=2.3.2 -DskipTests=true clean install
```

Nella cartella **target** del progetto viene creata la cartella **as7module**. Il contenuto di tale cartella deve essere copiato in `${JBOSS_HOME}/modules/system/layers/base`.  
Questa operazione copierà i due nuovi moduli rispettivamente nelle cartella
 - ${JBOSS_HOME}/modules/system/layers/base/id/au/ringerc/as7/eclipselinkintegration/main
 - ${JBOSS_HOME}/modules/system/layers/base/org/eclipse/persistence/main

Il contenuto dei file "module.xml" è il seguente:

```xml
<module xmlns="urn:jboss:module:1.1" name="id.au.ringerc.as7.eclipselinkintegration">
    <resources>
        <resource-root path="jboss-as-jpa-eclipselink-1.1.2.jar"/>
    </resources>

    <dependencies>
        <module name="javax.annotation.api"/>
        <module name="javax.persistence.api"/>
        <module name="javax.transaction.api"/>
        <module name="org.jboss.as.jpa.spi"/>
        <module name="org.jboss.vfs"/>
        <module name="org.jboss.logging"/>
        <module name="org.jboss.jandex"/>
        <module name="org.eclipse.persistence"/>
    </dependencies>
</module>
```

per Eclipselink

```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.1" name="org.eclipse.persistence">
    <resources>
        <resource-root path="eclipselink-2.3.2.jar"/>
    </resources>
    <dependencies>
        <!-- If the integration module is installed use it and expose it on the classpath
            of users of org.eclipse.persistence. This is necessary because EclipseLink will
            use their classloader to instantiate the logger classes, etc. -->
        <module name="id.au.ringerc.as7.eclipselinkintegration" export="true" optional="true"/>
        <module name="asm.asm"/>
        <module name="javax.api"/>
        <module name="javax.persistence.api"/>
        <module name="javax.transaction.api"/>
        <module name="javax.validation.api"/>
        <module name="javax.xml.bind.api"/>
        <module name="org.antlr"/>
        <module name="org.apache.ant" optional="true"/>
        <module name="org.apache.commons.collections"/>
        <module name="org.dom4j"/>
        <module name="org.javassist"/>
    </dependencies>
</module>
```

A questo punto è necessario configurare le seguenti variabili d'ambiente:

Chiave | Valore
--- | ---
eclipselink.archive.factory | id.au.ringerc.as7.eclipselinkintegration.JBossArchiveFactoryImpl
eclipselink.logging.logger | id.au.ringerc.as7.eclipselinkintegration.JBossLogger
eclipselink.target-server | JBoss

Per effettuare tale operazione è necessario avviare l'applicazione server (`${JBOSS_HOME}/bin/standalone.sh`) , effettuare l'accesso su http://localhost:9990/ con le credenziale configurate al [paragrafo di aggiunta dell'amministratore](#aggiunta-amministratore-application-server) e selezionare il pannello **Configuration**.  
In basso a sinistra selezionare **System Properties** ed aggiungere le proprietà di cui sopra.  
Nella configurazione in *domain* è possibile impostare le proprietà sia a livello globale sia a livello di singolo nodo.

### Configurazione data source / connection pool

Prima di aggiungere i datasource dalla console di amministrazione è necessario aggiungere i driver jdbc a JBoss.  
Il modulo contenente il jar di Oracle è stato aggiunto al paragrafo [Driver Oracle](#driver-oracle). Per renderlo disponibile ai datasource è necessario eseguire l'installazione dal command line di jboss; non è possibile eseguire tale operazione dalla console di amministrazione web.  
Eseguire, da `${JBOSS_HOME}/bin` il comando **jboss-cli.sh**. La procedura da eseguire **a server acceso** è la seguente:

```bash
/subsystem=datasources/jdbc-driver=ojdbc8/:add(driver-module-name=com.oracle.ojdbc8,driver-name=ojdbc8,driver-datasource-class-name=oracle.jdbc.OracleDriver,driver-xa-datasource-class-name=oracle.jdbc.xa.client.OracleXADataSource,jdbc-compliant=true)
```
Per la configurazione si rimanda alla documentazione specifica degli applicativi.  

## Configurazione del connettore jca XADisk

Nel file **XADisk.rar** fornito sono già stati configurati i due parametri relativi alla 
directory di lavoro e al nome istanza.  
Questi valori sono configurati come segue:

- **xaDiskHome** "../xaDiskHome" (N.B: la cartella deve essere raggiungibile e scrivibile
dall'utente che esegue l'application server).Bisogna accertarsi che la home xadisk non finisca nelle cartelle sincronizzate dall'application server (nel caso di installazione domain);
- **instanceId** "xaDisk1" .

La configurazione, dalla console web, viene effettuata nel modo seguente:

`Configuration > Subsystem > Resource Adapters`
 
`Add`

Nel sottoriquadro *Attributes*

Chiave | Valore 
------ | ------
Name | XADisk.rar
Archive | XADisk.rar
Module | - 
TX | XATransaction

Nel sottoriquadro *Properties*

Chiave | Valore 
--- | ------
instanceId | xaDisk1
synchronizeDirectoryChanges | true
xaDiskHome | ./xaDiskHome
enableRemoteInvocations | false

Sulla tabella dove compare la riga **XADisk.rar** alla colonna **Name** premere sul link **View** della colonna Option:

Aggiungere il seguente "connection definition":

Chiave | Valore 
--- | ------
Name | xaDiskPool
JNDI | java:/jca/xadiskLocal
Connection Class | org. xadisk. connector. outbound. XADiskManagedConnectionFactory

e le seguenti proprietà

Chiave | Valore 
--- | ------
instanceId | xaDisk1

Modificare la scheda pool definiendo **Min Pool Size** = 1 e **Max Pool Size** = 5.  
Eseguire ed abilitare (Enable attivo) il deploy del file **XADisk.rar** tramite la console di amministrazione sotto Deployments > add.  
Name e Runtime Name devono essere **XADisk.rar**.

## Configurazione Servizio JMS

Di tutte le applicazioni dell'ecosistema del ParER solo Sacerping e Sacer utilizzano il servizio di code JMS, quindi è necessario applicare la configurazione del servizio JMS descritta nei paragrafi successivi solo se si intende usare almeno una di queste due applicazioni. 

Per configurare il servizio di code JMS selezionare, tramite la console amministrativa

`Configuration > Messaging > Destinations`

### Configurazione del bilanciamento delle code

Il bilanciamento delle code permette di aumentare le perfomance del sistema distribuendo il carico tra i nodi JBoss.  
La formazione del cluster hornetq avviene sfruttando indirizzi di multicast dedicati ed è gestita dal subsystem jgroups.

#### Configurazione del subsystem messaging

Di seguito si riporta la configurazione del subsystem messaging di JBoss. Sono state evidenziate le sezioni modificate rispetto alle impostazioni di default.

```xml
<subsystem xmlns="urn:jboss:domain:messaging:1.4">
    <hornetq-server>
        <persistence-enabled>true</persistence-enabled>
```
<mark> Modificato rispetto al default </mark>
```xml
        <cluster-user>CLUSTER_USER</cluster-user>
        <cluster-password>CLUSTER_PASSWORD</cluster-password>
        <id-cache-size>2000</id-cache-size>
        <journal-type>ASYNCIO</journal-type>
```
```xml
        <journal-min-files>2</journal-min-files>
        <connectors>
            <netty-connector name="netty" socket-binding="messaging"/>
            <netty-connector name="netty-throughput" socket-binding="messaging-throughput">
                <param key="batch-delay" value="50"/>
            </netty-connector>
            <in-vm-connector name="in-vm" server-id="0"/>
        </connectors>

        <acceptors>
            <netty-acceptor name="netty" socket-binding="messaging"/>
            <netty-acceptor name="netty-throughput" socket-binding="messaging-throughput">
                <param key="batch-delay" value="50"/>
                <param key="direct-deliver" value="false"/>
            </netty-acceptor>
            <in-vm-acceptor name="in-vm" server-id="0"/>
        </acceptors>

        <broadcast-groups>
            <broadcast-group name="bg-group1">
```
<mark> Modificato rispetto al default </mark>
```xml
                <jgroups-stack>tcp</jgroups-stack>
                <jgroups-channel>hq-cluster</jgroups-channel>
```
```xml
                <broadcast-period>5000</broadcast-period>
                <connector-ref>
                    netty
                </connector-ref>
            </broadcast-group>
        </broadcast-groups>

        <discovery-groups>
            <discovery-group name="dg-group1">
```
<mark> Modificato rispetto al default </mark>
```xml
                <jgroups-stack>tcp</jgroups-stack>
                <jgroups-channel>hq-cluster</jgroups-channel>
```
```xml
                <refresh-timeout>10000</refresh-timeout>
            </discovery-group>
        </discovery-groups>

        <cluster-connections>
```
<mark> Modificato rispetto al default </mark>
```xml
            <cluster-connection name="parer-prod-cluster">
```
```xml
                <address>jms</address>
                <connector-ref>netty</connector-ref>
                <max-hops>1</max-hops>
                <discovery-group-ref discovery-group-name="dg-group1"/>
            </cluster-connection>
        </cluster-connections>

        <security-settings>
            <security-setting match="#">
                <permission type="send" roles="ParER guest"/>
                <permission type="consume" roles="ParER guest"/>
                <permission type="createNonDurableQueue" roles="guest"/>
                <permission type="deleteNonDurableQueue" roles="guest"/>
                <permission type="manage" roles="parer"/>
            </security-setting>
        </security-settings>

        <address-settings>
            <address-setting match="#">
                <dead-letter-address>jms.queue.DLQ</dead-letter-address>
                <expiry-address>jms.queue.ExpiryQueue</expiry-address>
                <redelivery-delay>0</redelivery-delay>
                <max-size-bytes>10485760</max-size-bytes>
                <page-size-bytes>2097152</page-size-bytes>
                <address-full-policy>PAGE</address-full-policy>
                <message-counter-history-day-limit>10</message-counter-history-day-limit>
                <redistribution-delay>1000</redistribution-delay>
            </address-setting>
        </address-settings>

        <jms-connection-factories>
            <connection-factory name="InVmConnectionFactory">
                <connectors>
                    <connector-ref connector-name="in-vm"/>
                </connectors>
                <entries>
                    <entry name="java:/ConnectionFactory"/>
                </entries>
            </connection-factory>
            <connection-factory name="RemoteConnectionFactory">
                <connectors>
                    <connector-ref connector-name="netty"/>
                </connectors>
                <entries>
                    <entry name="java:jboss/exported/jms/RemoteConnectionFactory"/>
                </entries>
                <ha>true</ha>
                <block-on-acknowledge>true</block-on-acknowledge>
                <retry-interval>1000</retry-interval>
                <retry-interval-multiplier>1.0</retry-interval-multiplier>
                <reconnect-attempts>-1</reconnect-attempts>
            </connection-factory>
```
<mark> Modificato rispetto al default </mark>
```xml
            <connection-factory name="InVmConnectionFactoryXA">
                <factory-type>XA_GENERIC</factory-type>
                <connectors>
                    <connector-ref connector-name="in-vm"/>
                </connectors>
                <entries>
                    <entry name="java:/ConnectionFactoryXA"/>
                </entries>
            </connection-factory>
```
```xml
            <pooled-connection-factory name="hornetq-ra">
                <transaction mode="xa"/>
                <connectors>
                    <connector-ref connector-name="in-vm"/>
                </connectors>
                <entries>
                    <entry name="java:/JmsXA"/>
                </entries>
            </pooled-connection-factory>
```
<mark> Modificato rispetto al default </mark>
```xml
            <pooled-connection-factory name="sacer-hornetq-ra">
                <transaction mode="xa"/>
                <connectors>
                    <connector-ref connector-name="in-vm"/>
                </connectors>
                <entries>
                    <entry name="java:/SacerJmsXA"/>
                </entries>
            </pooled-connection-factory>
```
```xml
        </jms-connection-factories>

        <jms-destinations>
            <jms-queue name="ExpiryQueue">
                <entry name="java:/jms/queue/ExpiryQueue"/>
            </jms-queue>
            <jms-queue name="DLQ">
                <entry name="java:/jms/queue/DLQ"/>
            </jms-queue>
        </jms-destinations>
    </hornetq-server>
</subsystem>
```
Per configurare la pooled connection factory **sacer-hornetq-ra** dalla cli di JBoss digitare i seguenti comandi dopo essersi collegati all'ambiente da configurare:

```bash
/profile={my-profile}/subsystem=messaging/hornetq-server=default/pooled-connection-factory=sacer-hornetq-ra:add(connector={"in-vm" => undefined},entries=["java:/SacerJmsXA"],transaction="xa")
```

Per configurare la connection factory **ConnectionFactoryXA** dalla cli di JBoss utilizzata dal bridge JMS  i comandi della cli sono i seguenti:

```bash
/profile={my-profile}/subsystem=messaging/hornetq-server=default/connection-factory=InVmConnectionFactoryXA:add(connector={"in-vm" => undefined}, factory-type=XA_GENERIC,entries=["java:/ConnectionFactoryXA"])
```

La connection factory **ConnectionFactoryXA** è utilizzata dalla versione di SACER (5.5.0) che implementa il dialogo con i micro-servizi descritta nel paragrafo successivo.  
Sostituire `{my-profile}` con il profilo adeguato.

#### Configurazione del subsystem jgroups

Occorre modificare lo stack utilizzato da jgroups per la comunicazione tra i nodi del cluster.  
Di default viene utilizzato lo stack udp, la configurazione scelta per il ParER invece utilizza tcp.  
Il comando CLI per modificare lo stack di default è il seguente:

```bash
/profile=parer/subsystem=jgroups:write-attribute(name=default-stack,value=tcp)
```

#### System properties

Per il corretto funzionamento del cluster e la formazione dello stesso è fondamentale definire due system properties che controllano gli indirizzi di multicast utilizzati da hornetq e da jgroups.  
Le system properties sono

```
jboss.messaging.group.address
jboss.default.multicast.address
```

Gli indirizzi scelti devono essere unici per ogni cluster.  
Si riporta un esempio di comando CLI per configurare le system properties a livello di server-group

```bash
/server-group=parer-prod-A/system-property=jboss.messaging.group.address:add(value=231.8.8.8)
/server-group=parer-prod-A/system-property=jboss.default.multicast.address:add(value=230.0.0.5)
```

L'indirizzo di multicast scelto deve essere univoco all'interno della rete.

### Disattivazione del bilanciamento delle code

Il meccanismo di Load Balancing del cluster può essere spento settando a 0 l'attributo max-hops della cluster-connection definita:

```bash
/profile=parer/subsystem=messaging/hornetq-server=default/cluster-connection=parer-prod-cluster:write-attribute(name=max-hops,value=0)
```

Per riattivare il Load Balancing l'attributo max-hops va settato a 1:
```bash
/profile=parer/subsystem=messaging/hornetq-server=default/cluster-connection=parer-prod-cluster:write-attribute(name=max-hops,value=1)
```

### Configurazione Risorse JMS e Nomi JNDI

La modalità **full** predefinita di JBoss prevede le code **ExpiryQueue** e **DLQ**.  
Sono utilizzate per memorizzare i messaggi contenenti eccezioni e di default un messaggio prima di essere inserito nella coda **DLQ** (Dead Letter Queue) deve essere re-inviato 10 volte. 

## Utilizzo di Xadisk e NFS

Non è necessario montare le folder con il parametro “sync”: xadisk forza il fushing delle folder commit time tramite fsync.  
Discorso diverso per le cache di lookup: è necessario impostare il parametro `lookupcache=positive` per non mantenere la cache di entry non presenti sul server.  
In caso di migrazione da glassfish queste configurazioni sono già state effettuate come nel caso delle transazioni [XA](#configurazione-del-transaction-service), in quanto facenti parte di configurazioni specifiche di NFS e non dell'application server.

## Configurazione dei webservices

Il subsystem webservices necessita della modifica di 3 parametri che regolano la riscrittura del tag soap:address quando viene generato dinamicamente il wsdl.  
Nello specifico, vanno modificati come segue gli attributi **wsdl-port**, **wsdl-secure-port** e **wsdl-host**.  
Questi i comandi CLI da utilizzare:
```bash
/profile=parer/subsystem=webservices:write-attribute(name=wsdl-secure-port,value=443)
/profile=parer/subsystem=webservices:write-attribute(name=wsdl-port,value=80)
/profile=parer/subsystem=webservices:write-attribute(name=wsdl-host,value=jbossws.undefined.host)
```

## Configurazioni aggiuntive

Per il corretto funzionamento delle applicazioni è stato necessario aggiungere la seguente proprietà di sistema:

Chiave | Valore
--- | ---
org.apache.tomcat.util.http.Parameters.MAX_COUNT | 10.000

Tale proprietà fa sì che si possano passare più di 512 parametri (valore predefinito) tramite una richiesta GET o POST.

### Proprietà di sistema

#### Generali

Chiave | Valore di esempio | Descrizione
--- | --- | ---
fed-metadata-url | `<property name="fed-metadata-url" value="https://parer-svil.ente.regione.emr.it/saceriam/federationMetadata"/>` | Indirizzo dal quale scaricare il file federation metadata
Idp-identity-id | `<property name="idp-identity-id" value="https://parer-svil.ente.regione.emr.it/idp/shibboleth"/>` | Identificativo univoco dell’IDP all’interno della Federazione

#### Specifiche per applicazione 

La documentazione specifica di ogni applicazione riporta eventuali system properties da configurare per il suo corretto funzionamento.

## Logging profiles

Per standardizzare la scrittura dei log per le applicazioni rilasciate vengono utilizzati i logging-profile forniti da JBoss.  
Le configurazioni possono essere applicate e modificate "a caldo" e dipendono dal nome del profilo associato al server-group.  
Si rimanda alla documentazione dell'applicazione per il logging-profile specifico.

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

## Configurazione del listener HTTPS

Si riporta di seguito un esempio di configurazione del connettore https con le componenti native.

```xml
<connector name="https" protocol="HTTP/1.1" scheme="https" socket-binding="https" secure="true" enabled="true">
	<ssl name="ssl" key-alias="/etc/pki/RER/ente.regione.emr.it.key" password="XXXXXXX" certificate-key-file="/etc/pki/RER/ente.regione.emr.it.key" protocol="TLSv1,TLSv1.1,TLSv1.2" verify-client="false" certificate-file="/etc/pki/RER/ente.regione.emr.it.pem" ca-certificate-file="/etc/pki/ca-trust/source/anchors/Regione Emilia-Romagna CA.crt"/>
</connector>
```
## JBoss tuning

In questa sezione verranno presentate tutte le modifiche alla configurazione predefinita dell'application server atte a migliorare le performance delle applicazioni.
Al momento questa sezione è *"work in progress"*.

### Installazione delle componenti native

Le componenti native di JBoss permettono di ottenere le migliori performances dai subsystems **web**e **messaging** abilitando funzionalità specifiche come ASYNCIO per hornetq e la modalità APR per i connettori http/https.  
Scaricare lo zip con le componenti native per JBoss al seguente link:  

https://access.redhat.com/jbossnetwork/restricted/listSoftware.html?downloadType=distributions&product=appplatform&version=6.4

Il pacchetto per RHEL 7 è *Red Hat JBoss Enterprise Application Platform 6.4.0 Native Components for RHEL 7 x86_64*.  
Il pacchetto va scompattato nella directory contenente l’installazione di JBoss. Le istruzioni di installazione sono disponibili al seguente link:  

https://access.redhat.com/solutions/222023

### Validazione del datasource

Vedi tabella 6.9 di https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/sect-Datasource_Configuration.html .

La configurazione di base dei datasource in RER viene fatta con il seguente comando CLI:

```bash
data-source --profile=PROFILE add --name=DS_NAME --jndi-name=DS_JNDI --connection-url=DS_URL --user-name=DS_USER --password=DS_PASSWD --driver-name=ojdbc8 --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter --stale-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker --statistics-enabled=true --use-ccm=true --use-fast-fail=true --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker --validate-on-match=true --flush-strategy=FailingConnectionOnly --background-validation=false --enabled=true
```
### Parametri JVM

I parametri di base utilizzati in RER sono i seguenti:

```bash
-XX:+UseLargePages
-server
-XX:+DoEscapeAnalysis
-XX:+UseCompressedOops
-XX:+UseConcMarkSweepGC
-XX:+CMSClassUnloadingEnabled
-XX:+UseParNewGC
-XX:+ExplicitGCInvokesConcurrent
-XX:CMSInitiatingOccupancyFraction=80
-XX:CMSIncrementalSafetyFactor=20
-XX:+UseCMSInitiatingOccupancyOnly
-verbose:gc
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+UseGCLogFileRotation
-XX:NumberOfGCLogFiles=5
-XX:GCLogFileSize=3M
-XX:-TraceClassUnloading
-Xloggc:/opt/jboss-eap/gclogs/parer-prod_gc.log
```
### Modificare i livelli di logging

Per modificare i livelli di logging andare, dalla console di amministrazione, su 
`Configuration > Core > Logging`.  
Nel riquadro "handler" è possibile aggiungere console o file handler specifici per le esigenze.  
Nel riquadro "log categories" è possibile associare categorie specifiche di log da assegnare agli handler.

## Ulteriori configurazioni 

Fare riferimento ai documenti presenti su https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/.

## Versioni di riferimento

Questa tabella riassume le versioni dei componenti utilizzati per la configurazione di JBoss EAP.
Nome | Numero di versione | Descrizione
--- | --- | ---
Jboss EAP | JBoss AS release: 7.5.20.Final-redhat-1 "Janus" JBoss AS product: EAP 6.4.20.GA | Versione completa dell’application server e livello di patchset.
Eclipselink | 2.3.2 | Motore ORM utilizzato come modulo JBoss
Eclipselink integration | 1.1.2 | Modulo di integrazione di Eclipselink su JBoss
XaDisk | 1.2.2.5 | Transazioni XA su filesystem
Oracle Jdbc | 8 per Oracle 19 | Driver per accedere al DB oracle
Modulo ActiveMQ Artemis | 1.5.5 | Modulo utilizzato per implementare il bridge JMS
idp-jaas-rdbms | 0.0.6 | Modulo utilizzato per la personalizzazione delle azioni associate al login

\newpage

## Riferimenti {#biblio}
Descrizione | Link
--- | ---
Getting Started Guide | https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Getting_Started_Guide/index.html
Installation Guide | https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Installation_Guide/index.html
Guida di amministrazione e configurazione | https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Administration_and_Configuration_Guide/index.html
Guida di sviluppo | https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Development_Guide/index.html
API di riferimento | https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/API_Documentation/index.html

**Strumenti di terze parti**
Strumento | Descrizione | Dove recuperarlo
Driver jdbc di oracle | Driver per accedere al dbms oracle | http://www.oracle.com/technetwork/database/features/jdbc/default-2280470.html
Modulo di autenticazione e log DB | Estensione del security manager per l'identity provider | https://rersvn.ente.regione.emr.it/projects/parer-idp-rdbmslogin
XaDisk | Strumento per transazioni 2PC su dbms e filesystem | https://nexus.ente.regione.emr.it/repository/thirdparty/net/java/xadisk/xadisk/1.2.2.5/
Eclipselink e sua integrazione | Motore orm ed integrazione a JBoss | https://nexus.ente.regione.emr.it/repository/jboss/org/eclipse/persistence/eclipselink/2.3.2/ -  https://github.com/lsnidero/as7-eclipselink-integration