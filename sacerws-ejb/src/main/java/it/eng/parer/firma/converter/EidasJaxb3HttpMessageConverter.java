/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.firma.converter;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.MarshalException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

public class EidasJaxb3HttpMessageConverter extends EidasAbstractJaxb3HttpMessageConverter<Object> {

    private boolean supportDtd = false;

    private boolean processExternalEntities = false;

    public void setSupportDtd(boolean supportDtd) {
	this.supportDtd = supportDtd;
    }

    public boolean isSupportDtd() {
	return this.supportDtd;
    }

    public void setProcessExternalEntities(boolean processExternalEntities) {
	this.processExternalEntities = processExternalEntities;
	if (processExternalEntities) {
	    setSupportDtd(true);
	}
    }

    public boolean isProcessExternalEntities() {
	return this.processExternalEntities;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
	return (clazz.isAnnotationPresent(XmlRootElement.class)
		|| clazz.isAnnotationPresent(XmlType.class)) && canRead(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
	return (AnnotationUtils.findAnnotation(clazz, XmlRootElement.class) != null
		&& canWrite(mediaType));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
	// should not be called, since we override canRead/Write
	throw new UnsupportedOperationException();
    }

    @Override
    protected Object readFromSource(Class<?> clazz, HttpHeaders headers, Source source)
	    throws Exception {
	try {
	    source = processSource(source);
	    Unmarshaller unmarshaller = createUnmarshaller(clazz);
	    if (clazz.isAnnotationPresent(XmlRootElement.class)) {
		return unmarshaller.unmarshal(source);
	    } else {
		JAXBElement<?> jaxbElement = unmarshaller.unmarshal(source, clazz);
		return jaxbElement.getValue();
	    }
	} catch (NullPointerException ex) {
	    if (!isSupportDtd()) {
		throw new IllegalStateException("NPE while unmarshalling. "
			+ "This can happen due to the presence of DTD declarations which are disabled.",
			ex);
	    }
	    throw ex;
	} catch (UnmarshalException ex) {
	    throw ex;
	} catch (JAXBException ex) {
	    throw new HttpMessageConversionException("Invalid JAXB setup: " + ex.getMessage(), ex);
	}
    }

    protected Source processSource(Source source) {
	if (source instanceof StreamSource) {
	    StreamSource streamSource = (StreamSource) source;
	    InputSource inputSource = new InputSource(streamSource.getInputStream());
	    try {
		/*
		 * XMLReader istanziato con una SAXParserFactory in modo da settare a true la
		 * property namespaceAware, al fine di rendere il reader consapevole dei namespaces
		 * necessari alla lettura della response secondo la versione 1.5 di jaxb
		 */
		final SAXParserFactory sax = SAXParserFactory.newInstance();
		sax.setNamespaceAware(true);
		final XMLReader xmlReader = sax.newSAXParser().getXMLReader();
		xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl",
			!isSupportDtd());
		String featureName = "http://xml.org/sax/features/external-general-entities";
		xmlReader.setFeature(featureName, isProcessExternalEntities());
		if (!isProcessExternalEntities()) {
		    xmlReader.setEntityResolver(NO_OP_ENTITY_RESOLVER);
		}
		return new SAXSource(xmlReader, inputSource);
	    } catch (SAXException ex) {
		logger.warn("Processing of external entities could not be disabled", ex);
		return source;
	    } catch (ParserConfigurationException ex) {
		Logger.getLogger(EidasJaxb3HttpMessageConverter.class.getName()).log(Level.SEVERE,
			null, ex);
		return source;
	    }
	} else {
	    return source;
	}
    }

    @Override
    protected void writeToResult(Object o, HttpHeaders headers, Result result) throws IOException {
	try {
	    Class<?> clazz = ClassUtils.getUserClass(o);
	    Marshaller marshaller = createMarshaller(clazz);
	    setCharset(headers.getContentType(), marshaller);
	    marshaller.marshal(o, result);
	} catch (MarshalException ex) {
	    throw new HttpMessageNotWritableException(
		    "Could not marshal [" + o + "]: " + ex.getMessage(), ex);
	} catch (JAXBException ex) {
	    throw new HttpMessageConversionException("Invalid JAXB setup: " + ex.getMessage(), ex);
	}
    }

    private void setCharset(@Nullable MediaType contentType, Marshaller marshaller)
	    throws PropertyException {
	if (contentType != null && contentType.getCharset() != null) {
	    marshaller.setProperty(Marshaller.JAXB_ENCODING, contentType.getCharset().name());
	}
    }

    private static final EntityResolver NO_OP_ENTITY_RESOLVER = (publicId,
	    systemId) -> new InputSource(new StringReader(""));

}
