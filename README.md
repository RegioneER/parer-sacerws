# SACERWS

Fonte template redazione documento:  https://www.makeareadme.com/.


# Descrizione

SACERWS è il modulo del Sistema di Conservazione SACER che espone i web service REST di versamento dei Pacchetti di Versamento (detti anche SIP o PdV) nel sistema di conservazione SACER.

I SIP devono rispettare una determinata struttura dati e sono composti da due elementi: 

- INDICE SIP: un documento XML che contiene i metadati descrittivi dell’entità da versare e i Parametri di versamento.
- OGGETTO-DATI: una o più sequenze di bit (tipicamente in forma di file) da sottoporre a conservazione.



# Installazione

Requisiti minimi per installazione: 

- Sistema operativo : consigliato Linux server (in alternativa compatibilità con Windows server);
- Java versione 8 (OpenJDK / Oracle);
- JBoss 7 EAP;
- Oracle DB (versione consigliata 19c).

## Instalazione JDK 

Consigliata adozione della OpenJDK alla versione 8, guida all'installazione https://openjdk.org/install/.

## Setup application server (Jboss 7)

Richiesta l'esecuzione delle seguenti guide secondo l'ordine riportato di seguito: 

1. guida per la configurazione **base** di [guida 1](src/docs/JBoss7_configurazione_generale.md);
2. guida con le configurazioni **specifiche** per il contesto applicativo **SACERWS**  di [guida 2](src/docs/JBoss7_configurazione_sacerws.md).

### Deploy su JBoss 7

Di seguito le indicazioni per il rilascio su application server JBoss7: 

1. generazione dell'artifact attraverso tool maven, eseguire il seguente comando: 

   ```bash
   mvn package
   ```
   
2. viene generato l'artifact .ear all'interno del modulo sacerws-ear/target (e.g. sacerws-5.2.0.ear)
3. deploy dell'ear generato allo step 1 su JBoss 7 (vedi configurazione [setup JBoss7](#setup-application-server-jboss-7))


## Predisposizione database

L'applicazione utilizza come DBMS di riferimento Oracle DB (https://www.oracle.com/it/database/) alla versione, consigliata, **19c**. Per l'installazione e la configurazione fare riferimento alle guide ufficiali.

Per la creazione del modello E-R consultare il seguente [README.md](https://github.com/RegioneER/parer-db-init/blob/master/README.md) (progetto di riferimento https://github.com/RegioneER/parer-db-init).


## Configurazione bucket S3 Object Storage

L'applicazione può essere integrata sulla base delle configurazioni impostate, vedi paragrafo [Setup application server (Jboss 7)](#setup-application-server-jboss-7), ad uno storage grid o object storage attraverso lo standard [AWS S3](https://aws.amazon.com/it/s3/).

In questa sezione si vuole proporre un possibile scenario di configurazione dei bucket e, in particolar modo, di eventuali [lifecycle](https://docs.aws.amazon.com/AmazonS3/latest/userguide/object-lifecycle-mgmt.html) legati ad alcuni bucket, essenziali per gestire in modo corretto ed opportuno il ciclo di vita degli oggetti all'interno di determinati bucket; su quest'ultimo aspetto, si sottolinea, che alcuni flussi applicativi/servizi prevedono la creazione di oggetto "temporanei" ossia la cui esistenza è prevista per un determinato periodo temporale dopo il quale possono essere eliminati dal bucket stesso.

### Configurazione dei bucket

Una possibile configurazione dei bucket, proposta, è la seguente: 

- aggiornamenti-metadati : oggetti prodotti dal servizio di Aggiornamento Metadati (vedi README.md progetto "sacerws");
- aip : oggetti prodotti dai processi di creazione dell'AIP;
- componenti : oggetti prodotti dai servizi di Versamento Unità Documentarie/Aggiunta documenti (vedi README.md progetto "sacerws");
- indici-aip : oggetti prodotti dai processi di creazione degli indici AIP;
- reportvf : oggetti prodotti dalla creazione di report verifica firma su documenti processati dai servizi di Versamento Unità Documentarie/Aggiunta documenti (vedi README.md progetto "sacerws");
- sessioni-agg-md-err-ko  : oggetti prodotti dal servizio di Aggiornamento Metadati (vedi README.md progetto "sacerws");
- vrs-staging : oggetti prodotti dai servizi di Versamento Unità Documentarie/Aggiunta documenti (vedi README.md progetto "sacerws").

I bucket possono essere creati con / senza versioning, alcuni dei bucket prevedono l'applicazione di lifecycle policy (consigliato), nello specifico: 
- vrs-staging;
- sessioni-agg-md-err-ko.

#### Lifecyle policy : casi d'uso

Bucket : vrs-staging

Esempio di lifecycle applicata (con filtri per tag)

```json
{
    "Rules": [
        {
            "Expiration": {
                "Days": 183
            },
            "ID": "default_no_tag",
            "Filter": {
                "Prefix": ""
            },
            "Status": "Enabled"
        },
        {
            "Expiration": {
                "Days": 92
            },
            "ID": "file_componente_uddoc",
            "Filter": {
                "And": {
                    "Prefix": "",
                    "Tags": [
                        {
                            "Key": "vrs-object-type",
                            "Value": "file_componente_uddoc"
                        }
                    ]
                }
            },
            "Status": "Enabled"
        },
        {
            "Expiration": {
                "Days": 183
            },
            "ID": "xml_metadati_uddoc",
            "Filter": {
                "And": {
                    "Prefix": "",
                    "Tags": [
                        {
                            "Key": "vrs-object-type",
                            "Value": "xml_metadati_uddoc"
                        }
                    ]
                }
            },
            "Status": "Enabled"
        },
        {
            "Expiration": {
                "Days": 30
            },
            "ID": "orphan_objects",
            "Filter": {
                "And": {
                    "Prefix": "",
                    "Tags": [
                        {
                            "Key": "orphan",
                            "Value": "true"
                        }
                    ]
                }
            },
            "Status": "Enabled"
        },
        {
            "Expiration": {
                "Days": 2
            },
            "ID": "temporany_object",
            "Filter": {
                "And": {
                    "Prefix": "",
                    "Tags": [
                        {
                            "Key": "vrs-object-type",
                            "Value": "temporany"
                        }
                    ]
                }
            },
            "Status": "Enabled"
        }
    ]
}
```

# Utilizzo

A seconda del modello di SIP utilizzato e delle sue caratteristiche, il SIP potrebbe essere composto solo dall’Indice SIP.

I SIP versati sono sottoposti a varie verifiche per essere accettati e presi in carico:
- Verifiche formali
- Verifiche semantiche
- Verifiche sul formato dei file versati
- Verifiche sulle firme digitali

I servizi esposti sono i seguenti:

- Versamento Unità documentarie (VersamentoSync): consente di versare in SACER un pacchetto di versamento contenente un’Unità documentaria;
- Aggiunta Documento (AggiuntaAllegatiSync): consente di versare in SACER un pacchetto di versamento contenente un Documento da aggiungere a un’Unità documentaria già presente nel sistema;
- Aggiornamento metadati Unità documentaria (AggiornamentoVersamentoSync): consente di versare in SACER un pacchetto di versamento contenente i metadati per aggiornare quelli di un’unità documentaria già presente nel sistema;
- Versamento Unità documentarie Multimedia (VersamentoMultiMedia): è una variante del servizio Versamento Unità documentarie in cui gli oggetti-dati (file) non sono trasmessi nella chiamata REST ma preventivamente collocati su un’area di lavoro e referenziati nell’Indice SIP;
- Versamento fascicolo (VersamentoFascicoloSync): consente di versare in SACER un pacchetto di versamento contenente un fascicolo.

Tutti i dettagli sulla composizione dei SIP, le modalità di invocazione dei servizi e le verifiche effettuate al versamento sono nei documenti di specifiche tecniche pubblicate a questo indirizzo: Documentazione — ParER — Polo archivistico dell'Emilia-Romagna (regione.emilia-romagna.it)

# Librerie utilizzate

|  GroupId | ArtifactId  | Version |
|:---:|:---:|:---:|
|none|||
|com.fasterxml.jackson.core|jackson-annotations|2.12.7.redhat-00003|
|com.fasterxml.jackson.core|jackson-core|2.12.7.redhat-00003|
|com.fasterxml.jackson.core|jackson-databind|2.12.7.redhat-00003|
|com.fasterxml.woodstox|woodstox-core|6.4.0.redhat-00001|
|com.fasterxml|classmate|1.5.1.redhat-00001|
|com.io7m.xom|xom|1.2.10|
|com.narupley|not-going-to-be-commons-ssl|0.3.20|
|com.sun.activation|jakarta.activation|1.2.2.redhat-00001|
|com.sun.istack|istack-commons-runtime|3.0.10.redhat-00001|
|com.sun.mail|jakarta.mail|1.6.7.redhat-00001|
|com.zaxxer|SparseBitSet|1.2|
|commons-beanutils|commons-beanutils|1.9.4|
|commons-codec|commons-codec|1.15|
|commons-fileupload|commons-fileupload|1.5|
|commons-io|commons-io|2.12.0|
|commons-logging|commons-logging|1.2|
|commons-net|commons-net|3.9.0|
|it.eng.parer|spagofat-core|5.12.0|
|it.eng.parer|spagofat-middle|5.12.0|
|it.eng.parer|spagofat-paginator-ejb|5.12.0|
|it.eng.parer|spagofat-paginator-gf|5.12.0|
|it.eng.parer|spagofat-sl-jpa|5.12.0|
|it.eng.parer|spagofat-timer-wrapper-common|5.12.0|
|jakarta.enterprise|jakarta.enterprise.cdi-api|2.0.2.redhat-00002|
|jakarta.inject|jakarta.inject-api|1.0.3.redhat-00001|
|jakarta.json.bind|jakarta.json.bind-api|1.0.2.redhat-00001|
|jakarta.json|jakarta.json-api|1.1.6.redhat-00001|
|jakarta.persistence|jakarta.persistence-api|2.2.3.redhat-00001|
|jakarta.security.enterprise|jakarta.security.enterprise-api|1.0.2.redhat-00001|
|jakarta.validation|jakarta.validation-api|2.0.2.redhat-00001|
|jakarta.xml.bind|jakarta.xml.bind-api|2.3.2|
|javax.activation|javax.activation-api|1.2.0|
|javax.jws|jsr181-api|1.0.0.MR1-redhat-8|
|javax.persistence|javax.persistence-api|2.2|
|javax.validation|validation-api|2.0.1.Final|
|javax.xml.bind|jaxb-api|2.3.0|
|joda-time|joda-time|2.12.5|
|net.bytebuddy|byte-buddy|1.11.12.redhat-00002|
|org.apache-extras.beanshell|bsh|2.0b6|
|org.apache.commons|commons-collections4|4.4|
|org.apache.commons|commons-lang3|3.12.0|
|org.apache.commons|commons-math3|3.6.1|
|org.apache.commons|commons-text|1.10.0|
|org.apache.httpcomponents|httpclient|4.5.14|
|org.apache.httpcomponents|httpcore|4.4.16|
|org.apache.poi|poi|4.1.2|
|org.apache.santuario|xmlsec|2.2.3.redhat-00001|
|org.apache.taglibs|taglibs-standard-impl|1.2.6.RC1-redhat-1|
|org.apache.taglibs|taglibs-standard-spec|1.2.6.RC1-redhat-1|
|org.apache.velocity|velocity-engine-core|2.0|
|org.apache.xmlbeans|xmlbeans|3.1.0|
|org.bouncycastle|bcpkix-jdk15on|1.70|
|org.bouncycastle|bcprov-jdk15on|1.70|
|org.bouncycastle|bcutil-jdk15on|1.70|
|org.codehaus.jettison|jettison|1.5.4|
|org.codehaus.woodstox|stax2-api|4.2.1.redhat-00001|
|org.dom4j|dom4j|2.1.3.redhat-00001|
|org.eclipse.persistence|org.eclipse.persistence.antlr|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.asm|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.core|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.moxy|2.3.2|
|org.glassfish.jaxb|jaxb-runtime|2.3.3.b02-redhat-00002|
|org.glassfish.jaxb|txw2|2.3.3.b02-redhat-00002|
|org.hibernate.common|hibernate-commons-annotations|5.0.5.Final-redhat-00002|
|org.hibernate.validator|hibernate-validator|6.0.22.Final-redhat-00002|
|org.hibernate|hibernate-core|5.3.20.Final-redhat-00001|
|org.hibernate|hibernate-jpamodelgen|5.6.14.Final-redhat-00001|
|org.javassist|javassist|3.27.0.GA-redhat-00001|
|org.jboss.logging|jboss-logging|3.4.1.Final-redhat-00001|
|org.jboss.spec.javax.annotation|jboss-annotations-api_1.3_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.batch|jboss-batch-api_1.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.ejb|jboss-ejb-api_3.2_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.el|jboss-el-api_3.0_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.enterprise.concurrent|jboss-concurrency-api_1.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.faces|jboss-jsf-api_2.3_spec|3.0.0.SP07-redhat-00001|
|org.jboss.spec.javax.interceptor|jboss-interceptors-api_1.2_spec|2.0.0.Final-redhat-00002|
|org.jboss.spec.javax.jms|jboss-jms-api_2.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.management.j2ee|jboss-j2eemgmt-api_1.1_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.resource|jboss-connector-api_1.7_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.security.auth.message|jboss-jaspi-api_1.1_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.security.jacc|jboss-jacc-api_1.5_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.servlet.jsp|jboss-jsp-api_2.3_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.servlet|jboss-servlet-api_4.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.transaction|jboss-transaction-api_1.2_spec|1.1.1.Final|
|org.jboss.spec.javax.transaction|jboss-transaction-api_1.3_spec|2.0.0.Final-redhat-00005|
|org.jboss.spec.javax.websocket|jboss-websocket-api_1.1_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.ws.rs|jboss-jaxrs-api_2.1_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.xml.bind|jboss-jaxb-api_2.3_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.xml.soap|jboss-saaj-api_1.4_spec|1.0.2.Final-redhat-00002|
|org.jboss.spec.javax.xml.ws|jboss-jaxws-api_2.3_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec|jboss-jakartaee-8.0|1.0.1.Final-redhat-00007|
|org.jboss|jandex|2.4.2.Final-redhat-00001|
|org.jboss|jboss-vfs|3.2.15.Final-redhat-00001|
|org.keycloak|keycloak-adapter-core|21.1.1|
|org.keycloak|keycloak-adapter-spi|21.1.1|
|org.keycloak|keycloak-authz-client|21.1.1|
|org.keycloak|keycloak-common|21.1.1|
|org.keycloak|keycloak-core|21.1.1|
|org.keycloak|keycloak-crypto-default|21.1.1|
|org.keycloak|keycloak-policy-enforcer|21.1.1|
|org.keycloak|keycloak-server-spi-private|21.1.1|
|org.keycloak|keycloak-server-spi|21.1.1|
|org.keycloak|keycloak-servlet-adapter-spi|21.1.1|
|org.keycloak|keycloak-servlet-filter-adapter|21.1.1|
|org.opensaml|opensaml|2.6.6|
|org.opensaml|openws|1.5.6|
|org.opensaml|xmltooling|1.4.6|
|org.owasp.esapi|esapi|2.2.0.0|
|org.slf4j|slf4j-api|2.0.7|
|org.springframework.security.extensions|spring-security-saml2-core|1.0.10.RELEASE|
|org.springframework.security|spring-security-config|5.8.8|
|org.springframework.security|spring-security-core|5.8.8|
|org.springframework.security|spring-security-crypto|5.8.8|
|org.springframework.security|spring-security-web|5.8.8|
|org.springframework|spring-aop|5.3.30|
|org.springframework|spring-beans|5.3.30|
|org.springframework|spring-context|5.3.30|
|org.springframework|spring-core|5.3.30|
|org.springframework|spring-expression|5.3.30|
|org.springframework|spring-jcl|5.3.30|
|org.springframework|spring-web|5.3.30|
|org.springframework|spring-webmvc|5.3.30|
|xml-apis|xml-apis|1.4.01|
|com.fasterxml.jackson.core|jackson-annotations|2.12.7.redhat-00003|
|com.fasterxml.jackson.core|jackson-core|2.12.7.redhat-00003|
|com.fasterxml.jackson.core|jackson-databind|2.12.7.redhat-00003|
|com.fasterxml.woodstox|woodstox-core|6.4.0.redhat-00001|
|com.google.code.findbugs|jsr305|3.0.2|
|com.google.guava|failureaccess|1.0.1.redhat-00002|
|com.google.guava|guava|30.1.0.redhat-00001|
|com.io7m.xom|xom|1.2.10|
|com.narupley|not-going-to-be-commons-ssl|0.3.20|
|com.oracle|ojdbc16|11.2.0.3.0|
|com.sun.activation|jakarta.activation|1.2.2.redhat-00001|
|com.sun.istack|istack-commons-runtime|3.0.10.redhat-00001|
|com.sun.mail|jakarta.mail|1.6.7.redhat-00001|
|com.sun.xml.bind|jaxb-core|2.3.0|
|com.sun.xml.bind|jaxb-impl|2.3.0|
|com.zaxxer|SparseBitSet|1.2|
|commons-beanutils|commons-beanutils-core|1.8.3|
|commons-beanutils|commons-beanutils|1.9.4|
|commons-cli|commons-cli|1.4|
|commons-codec|commons-codec|1.15|
|commons-collections|commons-collections|3.2.2|
|commons-dbcp|commons-dbcp|1.4|
|commons-digester|commons-digester|1.8|
|commons-fileupload|commons-fileupload|1.5|
|commons-io|commons-io|2.12.0|
|commons-lang|commons-lang|2.4|
|commons-logging|commons-logging|1.2|
|commons-net|commons-net|3.9.0|
|commons-pool|commons-pool|1.5.7|
|eu.europa.ec.joinup.sd-dss|dss-alert|5.13|
|eu.europa.ec.joinup.sd-dss|dss-common-remote-dto|5.13|
|eu.europa.ec.joinup.sd-dss|dss-crl-parser|5.13|
|eu.europa.ec.joinup.sd-dss|dss-detailed-report-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-diagnostic-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-document|5.13|
|eu.europa.ec.joinup.sd-dss|dss-enumerations|5.13|
|eu.europa.ec.joinup.sd-dss|dss-i18n|5.13|
|eu.europa.ec.joinup.sd-dss|dss-jaxb-common|5.13|
|eu.europa.ec.joinup.sd-dss|dss-jaxb-parsers|5.13|
|eu.europa.ec.joinup.sd-dss|dss-model|5.13|
|eu.europa.ec.joinup.sd-dss|dss-policy-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-simple-certificate-report-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-simple-report-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-spi|5.13|
|eu.europa.ec.joinup.sd-dss|dss-utils-apache-commons|5.13|
|eu.europa.ec.joinup.sd-dss|dss-utils|5.13|
|eu.europa.ec.joinup.sd-dss|dss-validation-dto|5.13|
|eu.europa.ec.joinup.sd-dss|dss-validation-soap-client|5.13|
|eu.europa.ec.joinup.sd-dss|dss-xml-common|5.13|
|eu.europa.ec.joinup.sd-dss|specs-trusted-list|5.13|
|eu.europa.ec.joinup.sd-dss|specs-validation-report|5.13|
|eu.europa.ec.joinup.sd-dss|specs-xades|5.13|
|eu.europa.ec.joinup.sd-dss|specs-xmldsig|5.13|
|eu.europa.ec.joinup.sd-dss|validation-policy|5.13|
|io.netty|netty-buffer|4.1.86.Final|
|io.netty|netty-codec-http2|4.1.86.Final|
|io.netty|netty-codec-http|4.1.86.Final|
|io.netty|netty-codec|4.1.86.Final|
|io.netty|netty-common|4.1.86.Final|
|io.netty|netty-handler|4.1.86.Final-redhat-00001|
|io.netty|netty-resolver|4.1.86.Final|
|io.netty|netty-transport-classes-epoll|4.1.86.Final|
|io.netty|netty-transport-native-unix-common|4.1.86.Final|
|io.netty|netty-transport|4.1.86.Final|
|it.eng.parer|idp-jaas-rdbms|0.0.9|
|it.eng.parer|parer-retry|2.1.0|
|it.eng.parer|sacer-xml|2.8.0|
|it.eng.parer|sacerws-jpa|5.3.1-SNAPSHOT|
|it.eng.parer|spagofat-core|5.12.0|
|it.eng.parer|spagofat-middle|5.12.0|
|it.eng.parer|spagofat-paginator-ejb|5.12.0|
|it.eng.parer|spagofat-paginator-gf|5.12.0|
|it.eng.parer|spagofat-timer-wrapper-common|5.12.0|
|it.eng.parer|verificafirma-crypto-beans|1.3.0|
|it.eng.parer|verificafirma-eidas-beans|1.9.0|
|jakarta.activation|jakarta.activation-api|1.2.2|
|jakarta.enterprise|jakarta.enterprise.cdi-api|2.0.2.redhat-00002|
|jakarta.inject|jakarta.inject-api|1.0.3.redhat-00001|
|jakarta.json.bind|jakarta.json.bind-api|1.0.2.redhat-00001|
|jakarta.json|jakarta.json-api|1.1.6.redhat-00001|
|jakarta.jws|jakarta.jws-api|2.1.0|
|jakarta.persistence|jakarta.persistence-api|2.2.3.redhat-00001|
|jakarta.security.enterprise|jakarta.security.enterprise-api|1.0.2.redhat-00001|
|jakarta.validation|jakarta.validation-api|2.0.2.redhat-00001|
|jakarta.xml.bind|jakarta.xml.bind-api|2.3.2|
|jakarta.xml.soap|jakarta.xml.soap-api|1.4.2|
|jakarta.xml.ws|jakarta.xml.ws-api|2.3.3|
|javax.inject|javax.inject|1|
|javax.jws|jsr181-api|1.0.0.MR1-redhat-8|
|javax.servlet|jstl|1.2|
|javax.validation|validation-api|1.0.0.GA|
|javax.xml.bind|jaxb-api|2.3.0|
|joda-time|joda-time|2.12.5|
|junit|junit|4.13.2|
|net.java.xadisk|xadisk|1.2.2.5|
|net.sourceforge.serp|serp|1.15.1|
|org.apache-extras.beanshell|bsh|2.0b6|
|org.apache.activemq.protobuf|activemq-protobuf|1.1|
|org.apache.activemq|activemq-broker|5.10.2|
|org.apache.activemq|activemq-client|5.10.2|
|org.apache.activemq|activemq-jdbc-store|5.10.2|
|org.apache.activemq|activemq-kahadb-store|5.10.2|
|org.apache.activemq|activemq-openwire-legacy|5.10.2|
|org.apache.activemq|activemq-ra|5.10.2|
|org.apache.bval|bval-core|0.5|
|org.apache.bval|bval-jsr303|0.5|
|org.apache.commons|commons-collections4|4.4|
|org.apache.commons|commons-compress|1.23.0|
|org.apache.commons|commons-lang3|3.12.0|
|org.apache.commons|commons-math3|3.6.1|
|org.apache.commons|commons-text|1.10.0|
|org.apache.felix|org.apache.felix.resolver|0.1.0.Beta1|
|org.apache.geronimo.components|geronimo-connector|3.1.1|
|org.apache.geronimo.components|geronimo-transaction|3.1.1|
|org.apache.geronimo.javamail|geronimo-javamail_1.4_mail|1.9.0-alpha-2|
|org.apache.geronimo.specs|geronimo-j2ee-connector_1.6_spec|1.0|
|org.apache.geronimo.specs|geronimo-j2ee-deployment_1.1_spec|1.1|
|org.apache.httpcomponents|httpclient|4.5.14|
|org.apache.httpcomponents|httpcore|4.4.16|
|org.apache.httpcomponents|httpmime|4.5.14|
|org.apache.maven.wagon|wagon-file|3.3.4|
|org.apache.maven.wagon|wagon-http-lightweight|2.6|
|org.apache.maven.wagon|wagon-http-shared|2.6|
|org.apache.maven.wagon|wagon-provider-api|3.3.4|
|org.apache.maven|maven-aether-provider|3.2.5|
|org.apache.maven|maven-artifact|3.6.3|
|org.apache.maven|maven-builder-support|3.6.3|
|org.apache.maven|maven-model-builder|3.6.3|
|org.apache.maven|maven-model|3.6.3|
|org.apache.maven|maven-repository-metadata|3.6.3|
|org.apache.maven|maven-settings-builder|3.6.3|
|org.apache.maven|maven-settings|3.6.3|
|org.apache.myfaces.core|myfaces-api|2.1.17|
|org.apache.myfaces.core|myfaces-impl|2.1.17|
|org.apache.openejb.shade|quartz-openejb-shade|2.2.1|
|org.apache.openejb|javaee-api|6.0-6|
|org.apache.openejb|javaee-api|tomcat|
|org.apache.openejb|mbean-annotation-api|4.7.5|
|org.apache.openejb|openejb-api|4.7.5|
|org.apache.openejb|openejb-client|4.7.5|
|org.apache.openejb|openejb-core|4.7.5|
|org.apache.openejb|openejb-ejbd|4.7.5|
|org.apache.openejb|openejb-http|4.7.5|
|org.apache.openejb|openejb-javaagent|4.7.5|
|org.apache.openejb|openejb-jee-accessors|4.7.5|
|org.apache.openejb|openejb-jee|4.7.5|
|org.apache.openejb|openejb-jpa-integration|4.7.5|
|org.apache.openejb|openejb-junit|4.7.5|
|org.apache.openejb|openejb-loader|4.7.5|
|org.apache.openejb|openejb-server|4.7.5|
|org.apache.openejb|tomee-catalina|1.7.5|
|org.apache.openejb|tomee-common|1.7.5|
|org.apache.openejb|tomee-embedded|1.7.5|
|org.apache.openejb|tomee-jdbc|1.7.5|
|org.apache.openejb|tomee-juli|1.7.5|
|org.apache.openejb|tomee-loader|1.7.5|
|org.apache.openejb|tomee-myfaces|1.7.5|
|org.apache.openjpa|openjpa|2.4.2|
|org.apache.openwebbeans|openwebbeans-ee-common|1.2.8|
|org.apache.openwebbeans|openwebbeans-ee|1.2.8|
|org.apache.openwebbeans|openwebbeans-ejb|1.2.8|
|org.apache.openwebbeans|openwebbeans-el22|1.2.8|
|org.apache.openwebbeans|openwebbeans-impl|1.2.8|
|org.apache.openwebbeans|openwebbeans-jsf|1.2.8|
|org.apache.openwebbeans|openwebbeans-spi|1.2.8|
|org.apache.openwebbeans|openwebbeans-web|1.2.8|
|org.apache.poi|poi|4.1.2|
|org.apache.santuario|xmlsec|2.2.3.redhat-00001|
|org.apache.taglibs|taglibs-standard-impl|1.2.6.RC1-redhat-1|
|org.apache.taglibs|taglibs-standard-spec|1.2.6.RC1-redhat-1|
|org.apache.tomcat|tomcat-annotations-api|7.0.81|
|org.apache.tomcat|tomcat-api|7.0.81|
|org.apache.tomcat|tomcat-catalina-ha|7.0.81|
|org.apache.tomcat|tomcat-catalina|7.0.81|
|org.apache.tomcat|tomcat-coyote|7.0.81|
|org.apache.tomcat|tomcat-dbcp|7.0.81|
|org.apache.tomcat|tomcat-el-api|7.0.81|
|org.apache.tomcat|tomcat-jasper-el|7.0.81|
|org.apache.tomcat|tomcat-jasper|7.0.81|
|org.apache.tomcat|tomcat-jdbc|7.0.81|
|org.apache.tomcat|tomcat-jsp-api|7.0.81|
|org.apache.tomcat|tomcat-juli|7.0.81|
|org.apache.tomcat|tomcat-servlet-api|7.0.81|
|org.apache.tomcat|tomcat-tribes|7.0.81|
|org.apache.tomcat|tomcat-util|7.0.81|
|org.apache.velocity|velocity-engine-core|2.0|
|org.apache.velocity|velocity|1.6.4|
|org.apache.xbean|xbean-asm5-shaded|4.2|
|org.apache.xbean|xbean-bundleutils|4.2|
|org.apache.xbean|xbean-finder-shaded|4.2|
|org.apache.xbean|xbean-naming|4.2|
|org.apache.xbean|xbean-reflect|4.2|
|org.apache.xmlbeans|xmlbeans|3.1.0|
|org.bouncycastle|bcpkix-jdk15on|1.70|
|org.bouncycastle|bcpkix-jdk18on|1.76|
|org.bouncycastle|bcprov-jdk15on|1.70|
|org.bouncycastle|bcprov-jdk18on|1.76|
|org.bouncycastle|bcutil-jdk15on|1.70|
|org.bouncycastle|bcutil-jdk18on|1.76|
|org.codehaus.jettison|jettison|1.5.4|
|org.codehaus.plexus|plexus-interpolation|1.25|
|org.codehaus.plexus|plexus-utils|3.2.1|
|org.codehaus.swizzle|swizzle-stream|1.6.2|
|org.codehaus.woodstox|stax2-api|4.2.1.redhat-00001|
|org.dom4j|dom4j|2.1.3.redhat-00001|
|org.eclipse.aether|aether-api|1.0.0.v20140518|
|org.eclipse.aether|aether-connector-basic|1.0.0.v20140518|
|org.eclipse.aether|aether-impl|1.0.0.v20140518|
|org.eclipse.aether|aether-spi|1.0.0.v20140518|
|org.eclipse.aether|aether-transport-wagon|1.0.0.v20140518|
|org.eclipse.aether|aether-util|1.0.0.v20140518|
|org.eclipse.jdt.core.compiler|ecj|3.5.1|
|org.eclipse.persistence|org.eclipse.persistence.antlr|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.asm|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.core|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.moxy|2.3.2|
|org.eclipse.sisu|org.eclipse.sisu.inject|0.3.4|
|org.fusesource.hawtbuf|hawtbuf|1.10|
|org.fusesource.jansi|jansi|1.17.1|
|org.glassfish.jaxb|jaxb-runtime|2.3.3.b02-redhat-00002|
|org.glassfish.jaxb|txw2|2.3.3.b02-redhat-00002|
|org.hamcrest|hamcrest-all|1.3|
|org.hamcrest|hamcrest-core|1.3|
|org.hibernate|hibernate-jpamodelgen|5.6.14.Final-redhat-00001|
|org.hibernate|hibernate-validator|4.2.0.Final|
|org.hsqldb|hsqldb|2.3.2|
|org.jboss.arquillian.config|arquillian-config-api|1.4.0.Final|
|org.jboss.arquillian.config|arquillian-config-impl-base|1.4.0.Final|
|org.jboss.arquillian.config|arquillian-config-spi|1.4.0.Final|
|org.jboss.arquillian.container|arquillian-container-impl-base|1.4.0.Final|
|org.jboss.arquillian.container|arquillian-container-osgi|1.0.2.Final|
|org.jboss.arquillian.container|arquillian-container-spi|1.4.0.Final|
|org.jboss.arquillian.container|arquillian-container-test-api|1.4.0.Final|
|org.jboss.arquillian.container|arquillian-container-test-impl-base|1.4.0.Final|
|org.jboss.arquillian.container|arquillian-container-test-spi|1.4.0.Final|
|org.jboss.arquillian.core|arquillian-core-api|1.4.0.Final|
|org.jboss.arquillian.core|arquillian-core-impl-base|1.4.0.Final|
|org.jboss.arquillian.core|arquillian-core-spi|1.4.0.Final|
|org.jboss.arquillian.junit|arquillian-junit-container|1.4.0.Final|
|org.jboss.arquillian.junit|arquillian-junit-core|1.4.0.Final|
|org.jboss.arquillian.protocol|arquillian-protocol-jmx|1.4.0.Final|
|org.jboss.arquillian.protocol|arquillian-protocol-servlet|1.4.0.Final|
|org.jboss.arquillian.test|arquillian-test-api|1.4.0.Final|
|org.jboss.arquillian.test|arquillian-test-impl-base|1.4.0.Final|
|org.jboss.arquillian.test|arquillian-test-spi|1.4.0.Final|
|org.jboss.arquillian.testenricher|arquillian-testenricher-cdi|1.4.0.Final|
|org.jboss.arquillian.testenricher|arquillian-testenricher-ejb|1.4.0.Final|
|org.jboss.arquillian.testenricher|arquillian-testenricher-initialcontext|1.4.0.Final|
|org.jboss.arquillian.testenricher|arquillian-testenricher-osgi|1.0.2.Final|
|org.jboss.arquillian.testenricher|arquillian-testenricher-resource|1.4.0.Final|
|org.jboss.as|jboss-as-arquillian-common|7.1.1.Final|
|org.jboss.as|jboss-as-arquillian-container-managed|7.1.1.Final|
|org.jboss.as|jboss-as-arquillian-protocol-jmx|7.1.1.Final|
|org.jboss.as|jboss-as-arquillian-testenricher-msc|7.1.1.Final|
|org.jboss.as|jboss-as-build-config|7.1.1.Final|
|org.jboss.as|jboss-as-controller-client|7.1.1.Final|
|org.jboss.as|jboss-as-controller|7.1.1.Final|
|org.jboss.as|jboss-as-deployment-repository|7.1.1.Final|
|org.jboss.as|jboss-as-domain-http-interface|7.1.1.Final|
|org.jboss.as|jboss-as-domain-management|7.1.1.Final|
|org.jboss.as|jboss-as-ee|7.1.1.Final|
|org.jboss.as|jboss-as-embedded|7.1.1.Final|
|org.jboss.as|jboss-as-jmx|7.1.1.Final|
|org.jboss.as|jboss-as-naming|7.1.1.Final|
|org.jboss.as|jboss-as-network|7.1.1.Final|
|org.jboss.as|jboss-as-osgi-service|7.1.1.Final|
|org.jboss.as|jboss-as-platform-mbean|7.1.1.Final|
|org.jboss.as|jboss-as-process-controller|7.1.1.Final|
|org.jboss.as|jboss-as-protocol|7.1.1.Final|
|org.jboss.as|jboss-as-remoting|7.1.1.Final|
|org.jboss.as|jboss-as-server|7.1.1.Final|
|org.jboss.as|jboss-as-threads|7.1.1.Final|
|org.jboss.com.sun.httpserver|httpserver|1.0.0.Final|
|org.jboss.interceptor|jboss-interceptor-spi|2.0.0.Final|
|org.jboss.invocation|jboss-invocation|1.1.1.Final|
|org.jboss.logging|jboss-logging|3.4.1.Final-redhat-00001|
|org.jboss.logmanager|jboss-logmanager-log4j|1.0.0.GA|
|org.jboss.logmanager|jboss-logmanager|1.2.2.GA|
|org.jboss.marshalling|jboss-marshalling-river|1.3.11.GA|
|org.jboss.marshalling|jboss-marshalling|1.3.9.GA|
|org.jboss.metadata|jboss-metadata-common|7.0.1.Final|
|org.jboss.metadata|jboss-metadata-ear|7.0.1.Final|
|org.jboss.modules|jboss-modules|1.1.1.GA|
|org.jboss.msc|jboss-msc|1.4.12.Final-redhat-00001|
|org.jboss.osgi.deployment|jbosgi-deployment|1.0.12.Final|
|org.jboss.osgi.framework|jbosgi-framework-core|1.1.8.Final|
|org.jboss.osgi.metadata|jbosgi-metadata|2.0.3.Final|
|org.jboss.osgi.repository|jbosgi-repository-api|1.0.5|
|org.jboss.osgi.repository|jbosgi-repository-core|1.0.5|
|org.jboss.osgi.resolver|jbosgi-resolver-api-v2|2.0.0.Beta2|
|org.jboss.osgi.resolver|jbosgi-resolver-api|1.0.13.Final|
|org.jboss.osgi.resolver|jbosgi-resolver-felix|1.0.13.Final|
|org.jboss.osgi.resolver|jbosgi-resolver-spi|1.0.13.Final|
|org.jboss.osgi.spi|jbosgi-spi|3.0.1.Final|
|org.jboss.osgi.vfs|jbosgi-vfs30|1.0.7.Final|
|org.jboss.osgi.vfs|jbosgi-vfs|1.0.7.Final|
|org.jboss.remoting3|jboss-remoting|3.2.3.GA|
|org.jboss.remotingjmx|remoting-jmx|1.0.2.Final|
|org.jboss.sasl|jboss-sasl|1.0.0.Final|
|org.jboss.shrinkwrap.descriptors|shrinkwrap-descriptors-api-base|2.0.0|
|org.jboss.shrinkwrap.descriptors|shrinkwrap-descriptors-spi|2.0.0|
|org.jboss.shrinkwrap.resolver|shrinkwrap-resolver-api-maven|2.2.6|
|org.jboss.shrinkwrap.resolver|shrinkwrap-resolver-api|2.2.6|
|org.jboss.shrinkwrap.resolver|shrinkwrap-resolver-impl-maven|2.2.6|
|org.jboss.shrinkwrap.resolver|shrinkwrap-resolver-spi-maven|2.2.6|
|org.jboss.shrinkwrap.resolver|shrinkwrap-resolver-spi|2.2.6|
|org.jboss.shrinkwrap|shrinkwrap-api|1.2.6|
|org.jboss.shrinkwrap|shrinkwrap-impl-base|1.2.6|
|org.jboss.shrinkwrap|shrinkwrap-spi|1.2.6|
|org.jboss.spec.javax.annotation|jboss-annotations-api_1.1_spec|1.0.0.Final|
|org.jboss.spec.javax.annotation|jboss-annotations-api_1.3_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.batch|jboss-batch-api_1.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.ejb|jboss-ejb-api_3.2_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.el|jboss-el-api_3.0_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.enterprise.concurrent|jboss-concurrency-api_1.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.faces|jboss-jsf-api_2.3_spec|3.0.0.SP07-redhat-00001|
|org.jboss.spec.javax.interceptor|jboss-interceptors-api_1.1_spec|1.0.0.Final|
|org.jboss.spec.javax.interceptor|jboss-interceptors-api_1.2_spec|2.0.0.Final-redhat-00002|
|org.jboss.spec.javax.jms|jboss-jms-api_2.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.management.j2ee|jboss-j2eemgmt-api_1.1_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.resource|jboss-connector-api_1.7_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.security.auth.message|jboss-jaspi-api_1.1_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.security.jacc|jboss-jacc-api_1.5_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.servlet.jsp|jboss-jsp-api_2.3_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.servlet|jboss-servlet-api_4.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.transaction|jboss-transaction-api_1.1_spec|1.0.0.Final|
|org.jboss.spec.javax.transaction|jboss-transaction-api_1.3_spec|2.0.0.Final-redhat-00005|
|org.jboss.spec.javax.websocket|jboss-websocket-api_1.1_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.ws.rs|jboss-jaxrs-api_2.1_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.xml.bind|jboss-jaxb-api_2.3_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.xml.soap|jboss-saaj-api_1.4_spec|1.0.2.Final-redhat-00002|
|org.jboss.spec.javax.xml.ws|jboss-jaxws-api_2.3_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec|jboss-jakartaee-8.0|1.0.1.Final-redhat-00007|
|org.jboss.stdio|jboss-stdio|1.0.1.GA|
|org.jboss.threads|jboss-threads|2.4.0.Final-redhat-00001|
|org.jboss.xnio|xnio-api|3.8.9.Final-redhat-00001|
|org.jboss.xnio|xnio-nio|3.8.9.Final-redhat-00001|
|org.jboss|jandex|2.4.2.Final-redhat-00001|
|org.jboss|jboss-common-core|2.2.17.GA|
|org.jboss|jboss-dmr|1.1.1.Final|
|org.jboss|jboss-ejb-client|1.0.0.Final|
|org.jboss|jboss-remote-naming|1.0.2.Final|
|org.jboss|jboss-vfs|3.1.0.Final|
|org.jboss|staxmapper|1.1.0.Final|
|org.jsoup|jsoup|1.12.1|
|org.keycloak|keycloak-adapter-core|21.1.1|
|org.keycloak|keycloak-adapter-spi|21.1.1|
|org.keycloak|keycloak-authz-client|21.1.1|
|org.keycloak|keycloak-common|21.1.1|
|org.keycloak|keycloak-core|21.1.1|
|org.keycloak|keycloak-crypto-default|21.1.1|
|org.keycloak|keycloak-policy-enforcer|21.1.1|
|org.keycloak|keycloak-server-spi-private|21.1.1|
|org.keycloak|keycloak-server-spi|21.1.1|
|org.keycloak|keycloak-servlet-adapter-spi|21.1.1|
|org.keycloak|keycloak-servlet-filter-adapter|21.1.1|
|org.metatype.sxc|sxc-jaxb-core|0.8|
|org.metatype.sxc|sxc-runtime|0.8|
|org.objectweb.howl|howl|1.0.1-1|
|org.opensaml|opensaml|2.6.6|
|org.opensaml|openws|1.5.6|
|org.opensaml|xmltooling|1.4.6|
|org.osgi|org.osgi.compendium|4.2.0|
|org.osgi|org.osgi.core|4.2.0|
|org.osgi|org.osgi.enterprise|4.2.0|
|org.owasp.esapi|esapi|2.2.0.0|
|org.reactivestreams|reactive-streams|1.0.3.redhat-00003|
|org.slf4j|slf4j-api|2.0.7|
|org.slf4j|slf4j-jdk14|1.7.14|
|org.slf4j|slf4j-simple|2.0.7|
|org.sonatype.plexus|plexus-cipher|1.7|
|org.sonatype.plexus|plexus-sec-dispatcher|1.4|
|org.springframework.retry|spring-retry|1.3.3|
|org.springframework.security.extensions|spring-security-saml2-core|1.0.10.RELEASE|
|org.springframework.security|spring-security-config|5.8.8|
|org.springframework.security|spring-security-core|5.8.8|
|org.springframework.security|spring-security-crypto|5.8.8|
|org.springframework.security|spring-security-web|5.8.8|
|org.springframework|spring-aop|5.3.30|
|org.springframework|spring-beans|5.3.30|
|org.springframework|spring-context|5.3.30|
|org.springframework|spring-core|5.3.30|
|org.springframework|spring-expression|5.3.30|
|org.springframework|spring-jcl|5.3.30|
|org.springframework|spring-web|5.3.30|
|org.springframework|spring-webmvc|5.3.30|
|org.wildfly.client|wildfly-client-config|1.0.1.Final-redhat-00001|
|org.wildfly.common|wildfly-common|1.5.4.Final-redhat-00001|
|oro|oro|2.0.8|
|software.amazon.awssdk|annotations|2.20.87|
|software.amazon.awssdk|apache-client|2.20.87|
|software.amazon.awssdk|arns|2.20.87|
|software.amazon.awssdk|auth|2.20.87|
|software.amazon.awssdk|aws-core|2.20.87|
|software.amazon.awssdk|aws-query-protocol|2.20.87|
|software.amazon.awssdk|aws-xml-protocol|2.20.87|
|software.amazon.awssdk|crt-core|2.20.87|
|software.amazon.awssdk|endpoints-spi|2.20.87|
|software.amazon.awssdk|http-client-spi|2.20.87|
|software.amazon.awssdk|json-utils|2.20.87|
|software.amazon.awssdk|metrics-spi|2.20.87|
|software.amazon.awssdk|netty-nio-client|2.20.87|
|software.amazon.awssdk|profiles|2.20.87|
|software.amazon.awssdk|protocol-core|2.20.87|
|software.amazon.awssdk|regions|2.20.87|
|software.amazon.awssdk|s3|2.20.87|
|software.amazon.awssdk|sdk-core|2.20.87|
|software.amazon.awssdk|third-party-jackson-core|2.20.87|
|software.amazon.awssdk|utils|2.20.87|
|software.amazon.eventstream|eventstream|1.0.1|
|system|jdk-tools|jdk|
|xml-apis|xml-apis|1.4.01|
|com.fasterxml.jackson.core|jackson-annotations|2.12.7.redhat-00003|
|com.fasterxml.jackson.core|jackson-core|2.12.7.redhat-00003|
|com.fasterxml.jackson.core|jackson-databind|2.12.7.redhat-00003|
|com.fasterxml.woodstox|woodstox-core|6.4.0.redhat-00001|
|com.io7m.xom|xom|1.2.10|
|com.narupley|not-going-to-be-commons-ssl|0.3.20|
|com.sun.activation|jakarta.activation|1.2.2.redhat-00001|
|com.sun.istack|istack-commons-runtime|3.0.10.redhat-00001|
|com.sun.mail|jakarta.mail|1.6.7.redhat-00001|
|com.sun.xml.bind|jaxb-core|2.3.0|
|com.sun.xml.bind|jaxb-impl|2.3.0|
|com.zaxxer|SparseBitSet|1.2|
|commons-beanutils|commons-beanutils|1.9.4|
|commons-codec|commons-codec|1.15|
|commons-fileupload|commons-fileupload|1.5|
|commons-io|commons-io|2.12.0|
|commons-logging|commons-logging|1.2|
|commons-net|commons-net|3.9.0|
|eu.europa.ec.joinup.sd-dss|dss-alert|5.13|
|eu.europa.ec.joinup.sd-dss|dss-common-remote-dto|5.13|
|eu.europa.ec.joinup.sd-dss|dss-crl-parser|5.13|
|eu.europa.ec.joinup.sd-dss|dss-detailed-report-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-diagnostic-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-document|5.13|
|eu.europa.ec.joinup.sd-dss|dss-enumerations|5.13|
|eu.europa.ec.joinup.sd-dss|dss-i18n|5.13|
|eu.europa.ec.joinup.sd-dss|dss-jaxb-common|5.13|
|eu.europa.ec.joinup.sd-dss|dss-jaxb-parsers|5.13|
|eu.europa.ec.joinup.sd-dss|dss-model|5.13|
|eu.europa.ec.joinup.sd-dss|dss-policy-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-simple-certificate-report-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-simple-report-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-spi|5.13|
|eu.europa.ec.joinup.sd-dss|dss-utils-apache-commons|5.13|
|eu.europa.ec.joinup.sd-dss|dss-utils|5.13|
|eu.europa.ec.joinup.sd-dss|dss-validation-dto|5.13|
|eu.europa.ec.joinup.sd-dss|dss-validation-soap-client|5.13|
|eu.europa.ec.joinup.sd-dss|dss-xml-common|5.13|
|eu.europa.ec.joinup.sd-dss|specs-trusted-list|5.13|
|eu.europa.ec.joinup.sd-dss|specs-validation-report|5.13|
|eu.europa.ec.joinup.sd-dss|specs-xades|5.13|
|eu.europa.ec.joinup.sd-dss|specs-xmldsig|5.13|
|eu.europa.ec.joinup.sd-dss|validation-policy|5.13|
|io.netty|netty-buffer|4.1.86.Final|
|io.netty|netty-codec-http2|4.1.86.Final|
|io.netty|netty-codec-http|4.1.86.Final|
|io.netty|netty-codec|4.1.86.Final|
|io.netty|netty-common|4.1.86.Final|
|io.netty|netty-handler|4.1.86.Final-redhat-00001|
|io.netty|netty-resolver|4.1.86.Final|
|io.netty|netty-transport-classes-epoll|4.1.86.Final|
|io.netty|netty-transport-native-unix-common|4.1.86.Final|
|io.netty|netty-transport|4.1.86.Final|
|it.eng.parer|idp-jaas-rdbms|0.0.9|
|it.eng.parer|parer-retry|2.1.0|
|it.eng.parer|sacer-xml|2.8.0|
|it.eng.parer|sacerws-ejb|5.3.1-SNAPSHOT|
|it.eng.parer|sacerws-jpa|5.3.1-SNAPSHOT|
|it.eng.parer|spagofat-core|5.12.0|
|it.eng.parer|spagofat-middle|5.12.0|
|it.eng.parer|spagofat-paginator-ejb|5.12.0|
|it.eng.parer|spagofat-paginator-gf|5.12.0|
|it.eng.parer|spagofat-timer-wrapper-common|5.12.0|
|it.eng.parer|verificafirma-crypto-beans|1.3.0|
|it.eng.parer|verificafirma-eidas-beans|1.9.0|
|jakarta.activation|jakarta.activation-api|1.2.2|
|jakarta.enterprise|jakarta.enterprise.cdi-api|2.0.2.redhat-00002|
|jakarta.inject|jakarta.inject-api|1.0.3.redhat-00001|
|jakarta.json.bind|jakarta.json.bind-api|1.0.2.redhat-00001|
|jakarta.json|jakarta.json-api|1.1.6.redhat-00001|
|jakarta.jws|jakarta.jws-api|2.1.0|
|jakarta.persistence|jakarta.persistence-api|2.2.3.redhat-00001|
|jakarta.security.enterprise|jakarta.security.enterprise-api|1.0.2.redhat-00001|
|jakarta.validation|jakarta.validation-api|2.0.2.redhat-00001|
|jakarta.xml.bind|jakarta.xml.bind-api|2.3.2|
|jakarta.xml.soap|jakarta.xml.soap-api|1.4.2|
|jakarta.xml.ws|jakarta.xml.ws-api|2.3.3|
|javax.jws|jsr181-api|1.0.0.MR1-redhat-8|
|javax.xml.bind|jaxb-api|2.3.0|
|joda-time|joda-time|2.12.5|
|org.apache-extras.beanshell|bsh|2.0b6|
|org.apache.commons|commons-collections4|4.4|
|org.apache.commons|commons-compress|1.23.0|
|org.apache.commons|commons-lang3|3.12.0|
|org.apache.commons|commons-math3|3.6.1|
|org.apache.commons|commons-text|1.10.0|
|org.apache.httpcomponents|httpclient|4.5.14|
|org.apache.httpcomponents|httpcore|4.4.16|
|org.apache.httpcomponents|httpmime|4.5.14|
|org.apache.poi|poi|4.1.2|
|org.apache.santuario|xmlsec|2.2.3.redhat-00001|
|org.apache.taglibs|taglibs-standard-impl|1.2.6.RC1-redhat-1|
|org.apache.taglibs|taglibs-standard-spec|1.2.6.RC1-redhat-1|
|org.apache.velocity|velocity-engine-core|2.0|
|org.apache.xmlbeans|xmlbeans|3.1.0|
|org.bouncycastle|bcpkix-jdk15on|1.70|
|org.bouncycastle|bcpkix-jdk18on|1.76|
|org.bouncycastle|bcprov-jdk15on|1.70|
|org.bouncycastle|bcprov-jdk18on|1.76|
|org.bouncycastle|bcutil-jdk15on|1.70|
|org.bouncycastle|bcutil-jdk18on|1.76|
|org.codehaus.jettison|jettison|1.5.4|
|org.codehaus.woodstox|stax2-api|4.2.1.redhat-00001|
|org.eclipse.persistence|org.eclipse.persistence.antlr|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.asm|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.core|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.moxy|2.3.2|
|org.glassfish.jaxb|jaxb-runtime|2.3.3.b02-redhat-00002|
|org.glassfish.jaxb|txw2|2.3.3.b02-redhat-00002|
|org.hibernate|hibernate-jpamodelgen|5.6.14.Final-redhat-00001|
|org.jboss.logging|jboss-logging|3.4.1.Final-redhat-00001|
|org.jboss.spec.javax.annotation|jboss-annotations-api_1.3_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.batch|jboss-batch-api_1.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.ejb|jboss-ejb-api_3.2_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.el|jboss-el-api_3.0_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.enterprise.concurrent|jboss-concurrency-api_1.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.faces|jboss-jsf-api_2.3_spec|3.0.0.SP07-redhat-00001|
|org.jboss.spec.javax.interceptor|jboss-interceptors-api_1.2_spec|2.0.0.Final-redhat-00002|
|org.jboss.spec.javax.jms|jboss-jms-api_2.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.management.j2ee|jboss-j2eemgmt-api_1.1_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.resource|jboss-connector-api_1.7_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.security.auth.message|jboss-jaspi-api_1.1_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.security.jacc|jboss-jacc-api_1.5_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.servlet.jsp|jboss-jsp-api_2.3_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.servlet|jboss-servlet-api_4.0_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.transaction|jboss-transaction-api_1.3_spec|2.0.0.Final-redhat-00005|
|org.jboss.spec.javax.websocket|jboss-websocket-api_1.1_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec.javax.ws.rs|jboss-jaxrs-api_2.1_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.xml.bind|jboss-jaxb-api_2.3_spec|2.0.1.Final-redhat-00001|
|org.jboss.spec.javax.xml.soap|jboss-saaj-api_1.4_spec|1.0.2.Final-redhat-00002|
|org.jboss.spec.javax.xml.ws|jboss-jaxws-api_2.3_spec|2.0.0.Final-redhat-00001|
|org.jboss.spec|jboss-jakartaee-8.0|1.0.1.Final-redhat-00007|
|org.keycloak|keycloak-adapter-core|21.1.1|
|org.keycloak|keycloak-adapter-spi|21.1.1|
|org.keycloak|keycloak-authz-client|21.1.1|
|org.keycloak|keycloak-common|21.1.1|
|org.keycloak|keycloak-core|21.1.1|
|org.keycloak|keycloak-crypto-default|21.1.1|
|org.keycloak|keycloak-policy-enforcer|21.1.1|
|org.keycloak|keycloak-server-spi-private|21.1.1|
|org.keycloak|keycloak-server-spi|21.1.1|
|org.keycloak|keycloak-servlet-adapter-spi|21.1.1|
|org.keycloak|keycloak-servlet-filter-adapter|21.1.1|
|org.opensaml|opensaml|2.6.6|
|org.opensaml|openws|1.5.6|
|org.opensaml|xmltooling|1.4.6|
|org.owasp.esapi|esapi|2.2.0.0|
|org.reactivestreams|reactive-streams|1.0.3.redhat-00003|
|org.slf4j|slf4j-api|2.0.7|
|org.springframework.retry|spring-retry|1.3.3|
|org.springframework.security.extensions|spring-security-saml2-core|1.0.10.RELEASE|
|org.springframework.security|spring-security-config|5.8.8|
|org.springframework.security|spring-security-core|5.8.8|
|org.springframework.security|spring-security-crypto|5.8.8|
|org.springframework.security|spring-security-web|5.8.8|
|org.springframework|spring-aop|5.3.30|
|org.springframework|spring-beans|5.3.30|
|org.springframework|spring-context|5.3.30|
|org.springframework|spring-core|5.3.30|
|org.springframework|spring-expression|5.3.30|
|org.springframework|spring-jcl|5.3.30|
|org.springframework|spring-web|5.3.30|
|org.springframework|spring-webmvc|5.3.30|
|software.amazon.awssdk|annotations|2.20.87|
|software.amazon.awssdk|apache-client|2.20.87|
|software.amazon.awssdk|arns|2.20.87|
|software.amazon.awssdk|auth|2.20.87|
|software.amazon.awssdk|aws-core|2.20.87|
|software.amazon.awssdk|aws-query-protocol|2.20.87|
|software.amazon.awssdk|aws-xml-protocol|2.20.87|
|software.amazon.awssdk|crt-core|2.20.87|
|software.amazon.awssdk|endpoints-spi|2.20.87|
|software.amazon.awssdk|http-client-spi|2.20.87|
|software.amazon.awssdk|json-utils|2.20.87|
|software.amazon.awssdk|metrics-spi|2.20.87|
|software.amazon.awssdk|netty-nio-client|2.20.87|
|software.amazon.awssdk|profiles|2.20.87|
|software.amazon.awssdk|protocol-core|2.20.87|
|software.amazon.awssdk|regions|2.20.87|
|software.amazon.awssdk|s3|2.20.87|
|software.amazon.awssdk|sdk-core|2.20.87|
|software.amazon.awssdk|third-party-jackson-core|2.20.87|
|software.amazon.awssdk|utils|2.20.87|
|software.amazon.eventstream|eventstream|1.0.1|
|xml-apis|xml-apis|1.4.01|
|com.fasterxml.jackson.core|jackson-annotations|2.12.7.redhat-00003|
|com.fasterxml.jackson.core|jackson-core|2.12.7.redhat-00003|
|com.fasterxml.jackson.core|jackson-databind|2.12.7.redhat-00003|
|com.fasterxml.woodstox|woodstox-core|6.4.0.redhat-00001|
|com.io7m.xom|xom|1.2.10|
|com.narupley|not-going-to-be-commons-ssl|0.3.20|
|com.sun.activation|jakarta.activation|1.2.2.redhat-00001|
|com.sun.istack|istack-commons-runtime|3.0.10.redhat-00001|
|com.sun.xml.bind|jaxb-core|2.3.0|
|com.sun.xml.bind|jaxb-impl|2.3.0|
|com.zaxxer|SparseBitSet|1.2|
|commons-beanutils|commons-beanutils|1.9.4|
|commons-codec|commons-codec|1.15|
|commons-fileupload|commons-fileupload|1.5|
|commons-io|commons-io|2.12.0|
|commons-logging|commons-logging|1.2|
|commons-net|commons-net|3.9.0|
|eu.europa.ec.joinup.sd-dss|dss-alert|5.13|
|eu.europa.ec.joinup.sd-dss|dss-common-remote-dto|5.13|
|eu.europa.ec.joinup.sd-dss|dss-crl-parser|5.13|
|eu.europa.ec.joinup.sd-dss|dss-detailed-report-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-diagnostic-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-document|5.13|
|eu.europa.ec.joinup.sd-dss|dss-enumerations|5.13|
|eu.europa.ec.joinup.sd-dss|dss-i18n|5.13|
|eu.europa.ec.joinup.sd-dss|dss-jaxb-common|5.13|
|eu.europa.ec.joinup.sd-dss|dss-jaxb-parsers|5.13|
|eu.europa.ec.joinup.sd-dss|dss-model|5.13|
|eu.europa.ec.joinup.sd-dss|dss-policy-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-simple-certificate-report-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-simple-report-jaxb|5.13|
|eu.europa.ec.joinup.sd-dss|dss-spi|5.13|
|eu.europa.ec.joinup.sd-dss|dss-utils-apache-commons|5.13|
|eu.europa.ec.joinup.sd-dss|dss-utils|5.13|
|eu.europa.ec.joinup.sd-dss|dss-validation-dto|5.13|
|eu.europa.ec.joinup.sd-dss|dss-validation-soap-client|5.13|
|eu.europa.ec.joinup.sd-dss|dss-xml-common|5.13|
|eu.europa.ec.joinup.sd-dss|specs-trusted-list|5.13|
|eu.europa.ec.joinup.sd-dss|specs-validation-report|5.13|
|eu.europa.ec.joinup.sd-dss|specs-xades|5.13|
|eu.europa.ec.joinup.sd-dss|specs-xmldsig|5.13|
|eu.europa.ec.joinup.sd-dss|validation-policy|5.13|
|io.netty|netty-buffer|4.1.86.Final|
|io.netty|netty-codec-http2|4.1.86.Final|
|io.netty|netty-codec-http|4.1.86.Final|
|io.netty|netty-codec|4.1.86.Final|
|io.netty|netty-common|4.1.86.Final|
|io.netty|netty-handler|4.1.86.Final-redhat-00001|
|io.netty|netty-resolver|4.1.86.Final|
|io.netty|netty-transport-classes-epoll|4.1.86.Final|
|io.netty|netty-transport-native-unix-common|4.1.86.Final|
|io.netty|netty-transport|4.1.86.Final|
|it.eng.parer|idp-jaas-rdbms|0.0.9|
|it.eng.parer|parer-retry|2.1.0|
|it.eng.parer|sacer-xml|2.8.0|
|it.eng.parer|sacerws-ejb|5.3.1-SNAPSHOT|
|it.eng.parer|sacerws-jpa|5.3.1-SNAPSHOT|
|it.eng.parer|sacerws-web|5.3.1-SNAPSHOT|
|it.eng.parer|spagofat-core|5.12.0|
|it.eng.parer|spagofat-middle|5.12.0|
|it.eng.parer|spagofat-paginator-ejb|5.12.0|
|it.eng.parer|spagofat-paginator-gf|5.12.0|
|it.eng.parer|spagofat-sl-jpa|5.12.0|
|it.eng.parer|spagofat-timer-wrapper-common|5.12.0|
|it.eng.parer|verificafirma-crypto-beans|1.3.0|
|it.eng.parer|verificafirma-eidas-beans|1.9.0|
|jakarta.activation|jakarta.activation-api|1.2.2|
|jakarta.jws|jakarta.jws-api|2.1.0|
|jakarta.xml.bind|jakarta.xml.bind-api|2.3.2|
|jakarta.xml.soap|jakarta.xml.soap-api|1.4.2|
|jakarta.xml.ws|jakarta.xml.ws-api|2.3.3|
|javax.xml.bind|jaxb-api|2.3.0|
|joda-time|joda-time|2.12.5|
|org.apache-extras.beanshell|bsh|2.0b6|
|org.apache.commons|commons-collections4|4.4|
|org.apache.commons|commons-compress|1.23.0|
|org.apache.commons|commons-lang3|3.12.0|
|org.apache.commons|commons-math3|3.6.1|
|org.apache.commons|commons-text|1.10.0|
|org.apache.httpcomponents|httpclient|4.5.14|
|org.apache.httpcomponents|httpcore|4.4.16|
|org.apache.httpcomponents|httpmime|4.5.14|
|org.apache.poi|poi|4.1.2|
|org.apache.santuario|xmlsec|2.2.3.redhat-00001|
|org.apache.velocity|velocity-engine-core|2.0|
|org.apache.xmlbeans|xmlbeans|3.1.0|
|org.bouncycastle|bcpkix-jdk15on|1.70|
|org.bouncycastle|bcpkix-jdk18on|1.76|
|org.bouncycastle|bcprov-jdk15on|1.70|
|org.bouncycastle|bcprov-jdk18on|1.76|
|org.bouncycastle|bcutil-jdk15on|1.70|
|org.bouncycastle|bcutil-jdk18on|1.76|
|org.codehaus.jettison|jettison|1.5.4|
|org.codehaus.woodstox|stax2-api|4.2.1.redhat-00001|
|org.eclipse.persistence|org.eclipse.persistence.antlr|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.asm|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.core|2.3.2|
|org.eclipse.persistence|org.eclipse.persistence.moxy|2.3.2|
|org.glassfish.jaxb|jaxb-runtime|2.3.3.b02-redhat-00002|
|org.glassfish.jaxb|txw2|2.3.3.b02-redhat-00002|
|org.hibernate|hibernate-jpamodelgen|5.6.14.Final-redhat-00001|
|org.jboss.logging|jboss-logging|3.4.1.Final-redhat-00001|
|org.keycloak|keycloak-adapter-core|21.1.1|
|org.keycloak|keycloak-adapter-spi|21.1.1|
|org.keycloak|keycloak-authz-client|21.1.1|
|org.keycloak|keycloak-common|21.1.1|
|org.keycloak|keycloak-core|21.1.1|
|org.keycloak|keycloak-crypto-default|21.1.1|
|org.keycloak|keycloak-policy-enforcer|21.1.1|
|org.keycloak|keycloak-server-spi-private|21.1.1|
|org.keycloak|keycloak-server-spi|21.1.1|
|org.keycloak|keycloak-servlet-adapter-spi|21.1.1|
|org.keycloak|keycloak-servlet-filter-adapter|21.1.1|
|org.opensaml|opensaml|2.6.6|
|org.opensaml|openws|1.5.6|
|org.opensaml|xmltooling|1.4.6|
|org.owasp.esapi|esapi|2.2.0.0|
|org.reactivestreams|reactive-streams|1.0.3.redhat-00003|
|org.slf4j|slf4j-api|2.0.7|
|org.springframework.retry|spring-retry|1.3.3|
|org.springframework.security.extensions|spring-security-saml2-core|1.0.10.RELEASE|
|org.springframework.security|spring-security-config|5.8.8|
|org.springframework.security|spring-security-core|5.8.8|
|org.springframework.security|spring-security-crypto|5.8.8|
|org.springframework.security|spring-security-web|5.8.8|
|org.springframework|spring-aop|5.3.30|
|org.springframework|spring-beans|5.3.30|
|org.springframework|spring-context|5.3.30|
|org.springframework|spring-core|5.3.30|
|org.springframework|spring-expression|5.3.30|
|org.springframework|spring-jcl|5.3.30|
|org.springframework|spring-web|5.3.30|
|org.springframework|spring-webmvc|5.3.30|
|software.amazon.awssdk|annotations|2.20.87|
|software.amazon.awssdk|apache-client|2.20.87|
|software.amazon.awssdk|arns|2.20.87|
|software.amazon.awssdk|auth|2.20.87|
|software.amazon.awssdk|aws-core|2.20.87|
|software.amazon.awssdk|aws-query-protocol|2.20.87|
|software.amazon.awssdk|aws-xml-protocol|2.20.87|
|software.amazon.awssdk|crt-core|2.20.87|
|software.amazon.awssdk|endpoints-spi|2.20.87|
|software.amazon.awssdk|http-client-spi|2.20.87|
|software.amazon.awssdk|json-utils|2.20.87|
|software.amazon.awssdk|metrics-spi|2.20.87|
|software.amazon.awssdk|netty-nio-client|2.20.87|
|software.amazon.awssdk|profiles|2.20.87|
|software.amazon.awssdk|protocol-core|2.20.87|
|software.amazon.awssdk|regions|2.20.87|
|software.amazon.awssdk|s3|2.20.87|
|software.amazon.awssdk|sdk-core|2.20.87|
|software.amazon.awssdk|third-party-jackson-core|2.20.87|
|software.amazon.awssdk|utils|2.20.87|
|software.amazon.eventstream|eventstream|1.0.1|
|xalan|serializer|2.7.2|
|xalan|xalan|2.7.2|
|xerces|xercesImpl|2.12.0|
|xml-apis|xml-apis|1.4.01|


## Lista licenze in uso


 * agpl_v3     : GNU Affero General Public License (AGPL) version 3.0
 * apache_v2   : Apache License version 2.0
 * bsd_2       : BSD 2-Clause License
 * bsd_3       : BSD 3-Clause License
 * cddl_v1     : COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
 * epl_only_v1 : Eclipse Public License - v 1.0
 * epl_only_v2 : Eclipse Public License - v 2.0
 * epl_v1      : Eclipse Public + Distribution License - v 1.0
 * epl_v2      : Eclipse Public License - v 2.0 with Secondary License
 * eupl_v1_1   : European Union Public License v1.1
 * fdl_v1_3    : GNU Free Documentation License (FDL) version 1.3
 * gpl_v1      : GNU General Public License (GPL) version 1.0
 * gpl_v2      : GNU General Public License (GPL) version 2.0
 * gpl_v3      : GNU General Public License (GPL) version 3.0
 * lgpl_v2_1   : GNU General Lesser Public License (LGPL) version 2.1
 * lgpl_v3     : GNU General Lesser Public License (LGPL) version 3.0
 * mit         : MIT-License
  
# Supporto

Mantainer del progetto è [Engineering Ingegneria Informatica S.p.A.](https://www.eng.it/).

# Contributi

Se interessati a crontribuire alla crescita del progetto potete scrivere all'indirizzo email <a href="mailto:areasviluppoparer@regione.emilia-romagna.it">areasviluppoparer@regione.emilia-romagna.it</a>.

# Credits

Progetto di proprietà di [Regione Emilia-Romagna](https://www.regione.emilia-romagna.it/) sviluppato a cura di [Engineering Ingegneria Informatica S.p.A.](https://www.eng.it/).

# Licenza

Questo progetto è rilasciato sotto licenza GNU Affero General Public License v3.0 or later ([LICENSE.txt](LICENSE.txt)).

# Appendice

## Documentazione aggiuntiva

Alcuni riferimenti:

- Documentazione: https://poloarchivistico.regione.emilia-romagna.it/documentazione
