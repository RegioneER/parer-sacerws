package it.eng.parer.ws.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.c14n.Canonicalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import it.eng.parer.exception.SacerWsException;
import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 *
 * @author sinatti_s
 */
public class XmlUtils {

    private XmlUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Charset getXmlEcondingDeclaration(String xmlSip)
            throws XMLStreamException, FactoryConfigurationError {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xmlSip));
        String encodingFromXMLDeclaration = xmlStreamReader.getCharacterEncodingScheme();
        return StringUtils.isNotBlank(encodingFromXMLDeclaration) ? Charset.forName(encodingFromXMLDeclaration)
                : StandardCharsets.UTF_8;
    }

    public static String unPrettyPrint(final String xmlSip) throws IOException, DocumentException {
        final StringWriter sw;
        final OutputFormat format = OutputFormat.createCompactFormat();
        // format
        format.setNewLineAfterDeclaration(false);
        final org.dom4j.Document document = DocumentHelper.parseText(xmlSip);
        sw = new StringWriter();
        final XMLWriter writer = new XMLWriter(sw, format);
        writer.write(document);

        return sw.toString();
    }

    public static String doCanonicalizzazioneXml(final String xml, final boolean unPrettyPrint)
            throws SacerWsException {
        String xmlOut = null;
        try {
            org.apache.xml.security.Init.init();
            //
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // XXE: This is the PRIMARY defense. If DTDs (doctypes) are disallowed,
            // almost all XML entity attacks are prevented
            final String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(FEATURE, true);
            // ... and these as well, per Timothy Morgan's 2014 paper:
            // "XML Schema, DTD, and Entity Attacks" (see reference below)
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            // ... and, per Timothy Morgan:
            // "If for some reason support for inline DOCTYPEs are a requirement, then
            // ensure the entity settings are disabled (as shown above) and beware that SSRF
            // attacks
            // (http://cwe.mitre.org/data/definitions/918.html) and denial
            // of service attacks (such as billion laughs or decompression bombs via "jar:")
            // are a risk."

            // MAC#14681 fix per gestione encoding XML
            String encodingFromXMLDeclaration = XmlUtils.getXmlEcondingDeclaration(xml).name();
            InputSource in = new InputSource(new ByteArrayInputStream(xml.getBytes(encodingFromXMLDeclaration)));
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(in);

            org.apache.xml.security.c14n.Canonicalizer canonicalizer = Canonicalizer
                    .getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            byte[] res = canonicalizer.canonicalizeSubtree(doc);
            xmlOut = new String(res, StandardCharsets.UTF_8);
            if (unPrettyPrint) {
                xmlOut = XmlUtils.unPrettyPrint(xmlOut);
            }
        } catch (Exception e) {
            throw new SacerWsException("Errore in fase di canonicalizzazione XML", e,
                    SacerWsErrorCategory.INTERNAL_ERROR);
        }
        return xmlOut;
    }
}
