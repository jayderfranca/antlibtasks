package net.kernelits.antlibtasks.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Funcoes utilitarias de XML
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class XMLUtils {

    // realiza o parse de um arquivo Xml
    public static Document parseXml(File xml) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xml);
        doc.normalizeDocument();
        return doc;
    }

    // obtem o root do xml
    public static Node getXmlRootNode(Document document) {

        Node node = document.getFirstChild();
        while (node.getNodeType() == Node.COMMENT_NODE)
            node = node.getNextSibling();
        return node;
    }

    // cria um arquivo XML baseado no Document com transformacao XSL
    public static void saveXml(Document document, File xml, File xsl) throws TransformerException, TransformerConfigurationException {

        Transformer transf = null;

        // transformacao com xslt
        if (xsl != null)
            transf = TransformerFactory.newInstance().newTransformer(new StreamSource(xsl));
        else
            transf = TransformerFactory.newInstance().newTransformer();

        transf.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource input = new DOMSource(document);
        StreamResult output = new StreamResult(xml);
        transf.transform(input, output);
    }

    // cria um arquivo XML baseado no Document
    public static void saveXml(Document document, File xml) throws TransformerException, TransformerConfigurationException {

        // transformacao sem xslt
        saveXml(document, xml, null);
    }

    // obtem um xml remoto
    public static Document getRemoteXml(URL url) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputStream stream = url.openStream();
        Document doc = db.parse(stream);
        stream.close();
        doc.normalizeDocument();
        return doc;
    }

    // retorna o path do targeNamespace
    public static String getTargetNamespace(Node node) throws URISyntaxException {

        String targetNamespace = "";

        if (node.getAttributes().getNamedItem("targetNamespace") != null) {
            URI uri = new URI(node.getAttributes().getNamedItem("targetNamespace").getNodeValue().trim());
            targetNamespace = getPathNamespace(uri);
        }
        return targetNamespace;
    }

    // obtem o path de um namespace
    public static String getPathNamespace(URI namespace) {

        String path = "";

        // para protocolos http e https, obtem o path
        if (CommonUtils.isInList(namespace.getScheme().toLowerCase(), "http", "https")) {

            // caso o path esteja vazio, valida a url para transformacao
            if (!namespace.getPath().equals("/"))

                path = namespace.getPath();

            else {

                // remote a porta
                // de www.openuri.org:80 para www.openuri.org
                ArrayList<String> hostname = new ArrayList<String>();
                // transforma www.openuri.com para um array de www, openuri, org
                hostname.addAll(Arrays.asList(namespace.getHost().split(":")[0].split("\\.")));

                // obtem os dois ultimos indices
                // retorno org/openuri
                path = hostname.get(hostname.size() - 1) + "/" + hostname.get(hostname.size() - 2);
            }

        } else {

            URI normalized = namespace.normalize();
            path = normalized.getScheme() + "/" + normalized.getSchemeSpecificPart();
        }

        if (path.charAt(0) == '/')
            path = path.substring(1);
        if (path.charAt(path.length() - 1) == '/')
            path = path.substring(0, path.length() - 1);

        return path;
    }

    // retorna a lista de nodes encontrados no document que faca acesso externo
    public static NodeList getNodeListExternalResource(Document document) throws XPathExpressionException {

        // xpath para obter includes e imports do document
        String xPathIncludesImports = "(//*[local-name()='include' and namespace-uri()='http://www.w3.org/2001/XMLSchema']|"
                + "//*[local-name()='import' and namespace-uri()='http://www.w3.org/2001/XMLSchema'])";

        // cria o objeto de xpath
        XPath xpath = XPathFactory.newInstance().newXPath();

        // busca os nodes
        return (NodeList) xpath.compile(xPathIncludesImports).evaluate(document, XPathConstants.NODESET);
    }

    // retorna todas urls externa incluidas no documento
    public static List<URL> getURLExternalResource(Document document) throws XPathExpressionException, MalformedURLException {

        // lista de urls
        List<URL> urls = new ArrayList<URL>();

        // busca os nodes
        NodeList list = getNodeListExternalResource(document);

        // loop dos itens
        for (int index = 0; index < list.getLength(); index++) {

            // obtem o element
            Element element = (Element) list.item(index);

            // valida se o mesmo possui schemaLocation
            if (element.hasAttribute("schemaLocation"))
                urls.add(new URL(element.getAttribute("schemaLocation")));
        }

        // retorno da lista preenchida
        return urls;
    }
}
