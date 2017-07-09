package net.kernelits.antlibtasks.ant.tasks;

import net.kernelits.antlibtasks.Elapsed;
import net.kernelits.antlibtasks.ant.types.sbget.Reference;
import net.kernelits.antlibtasks.ant.types.sbget.ReferenceList;
import net.kernelits.antlibtasks.ant.types.sbget.Remote;
import net.kernelits.antlibtasks.ant.types.sbget.RemoteList;
import net.kernelits.antlibtasks.utils.*;
import net.kernelits.antlibtasks.utils.XMLUtils;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Task para obter WSDL e XSDs remotamente
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class SBGetTask extends Task {

    // variaveis
    private File dirOutput = null;
    private boolean failonerror = true;
    private boolean verbose = false;
    private File dirXslt = null;
    private ArrayList<Remote> remotes = new ArrayList<Remote>();
    private ArrayList<RemoteList> remoteList = new ArrayList<RemoteList>();
    private ArrayList<Reference> references = new ArrayList<Reference>();
    private ArrayList<ReferenceList> referenceList = new ArrayList<ReferenceList>();

    // sets
    public File getOutput() {
        return dirOutput;
    }

    public void setOutput(File dirOutput) {
        this.dirOutput = dirOutput;
    }

    public boolean getFailonerror() {
        return failonerror;
    }

    public void setFailonerror(boolean failonerror) {
        this.failonerror = failonerror;
    }

    public boolean getVerbose() {
        return failonerror;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public File getXslt() {
        return dirXslt;
    }

    public void setXslt(File dirXslt) {
        this.dirXslt = dirXslt;
    }

    // adiciona os types
    public void addRemote(Remote remote) {
        this.remotes.add(remote);
    }

    public void addRemoteList(RemoteList remoteList) {
        this.remoteList.add(remoteList);
    }

    public void addReference(Reference reference) {
        this.references.add(reference);
    }

    public void addReferenceList(ReferenceList referenceList) {
        this.referenceList.add(referenceList);
    }

    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {

        // valida os parametros desta task
        validate();

        try {

            // processa a lista de referencias
            for (ReferenceList list : referenceList)
                parseReferenceList(list.getFile(), references);

            // processa a lista de wsdls
            for (RemoteList list : remoteList)
                parseRemoteList(list.getFile(), remotes);

        } catch (Exception ex) {

        }

        log.info("Excluindo diretorio " + dirOutput.getAbsolutePath());

        // remove todos arquivos do diretorio de saida
        FileUtils.delete(dirOutput);

        // recria o diretorio de saida
        dirOutput.mkdirs();

        log.info("\n");

        // tempo de processamento da lista inteira
        Elapsed elapsed = Elapsed.init();

        // cache de recursos processados
        ArrayList<String> processed = new ArrayList<String>();

        // loop dos servicos remotos
        for (Remote remote : remotes) {

            try {

                // obtem o wsdl e derivados
                saveWSDLResources(remote.getUrl(), remote.getPackage(), dirXslt, processed);

            } catch (Exception ex) {

                // caso failonerror esteja false, apenas mostra o erro sem parar o processo
                if (!failonerror)
                    log.error(ex.getMessage());
                else
                    buildException(ex.getMessage(), ex);
            }
        }

        // informa o tempo total
        log("Tempo total: " + elapsed.toString());
    }

    // valida os parametros da task
    public void validate() throws BuildException {

        // realiza as validacoes nos parametros
        if (dirOutput == null)
            throw new BuildException("Parametro output nao informado");
    }

    // realiza o parse do arquivo com a lista de wsdl
    public void parseRemoteList(File list, List<Remote> remotes) throws Exception {

        // realiza a leitura do arquivo para uma lista
        List<String> lines = FileUtils.read(list);

        // loop de cada linha para processamento
        for (String line : lines) {

            // valida se a linha nao eh um comentario
            if (line.replaceAll(";", "").trim().startsWith("#"))
                continue;

            // valida se a linha nao esta vazia
            if (line.replaceAll(";", "").trim().equals(""))
                continue;

            // separa a url da package
            String[] fields = line.trim().split(";");

            // obtem o wsdl
            String wsdl = fields[0];

            // obtem a package caso informado
            String packageWsdl = fields.length > 1 ? fields[1] : "";

            // cria um novo remote
            Remote remote = new Remote();
            remote.setUrl(new URL(wsdl));
            remote.setPackage(packageWsdl);

            // adiciona na lista
            remotes.add(remote);
        }

        // ordena a lista
        Collections.sort(remotes, new Comparator<Remote>() {
            public int compare(Remote left, Remote right) {
                return left.getUrl().toString().compareToIgnoreCase(right.getUrl().toString());
            }
        });
    }

    // realiza o parse do arquivo com a lista de referencia
    public void parseReferenceList(File list, List<Reference> references) throws Exception {

        // realiza a leitura do arquivo para uma lista
        List<String> lines = FileUtils.read(list);

        // loop de cada linha para processamento
        for (String line : lines) {

            // valida se a linha nao eh um comentario
            if (line.replaceAll("=", "").trim().startsWith("#"))
                continue;

            // valida se a linha nao esta vazia
            if (line.replaceAll("=", "").trim().equals(""))
                continue;

            // separa o de para
            String[] fields = line.trim().split("=");

            // obtem o de
            String from = fields[0];

            // obtem o para
            String to = fields.length > 1 ? fields[1] : "";

            // cria uma nova referencia
            Reference reference = new Reference();
            reference.setFrom(from);
            reference.setTo(to);

            // adiciona na lista
            references.add(reference);
        }

        // ordena a lista
        Collections.sort(references, new Comparator<Reference>() {
            public int compare(Reference left, Reference right) {
                return left.getFrom().toString().compareToIgnoreCase(right.getFrom().toString());
            }
        });
    }

    // busca por transformacao de referencia
    private String getFilenameReference(String filename) {

        // nome atual do arquivo
        String newFilename = filename;

        // percorre o hash de referencias
        for (Reference reference : references) {

            // caso encontre a referencia, substitui
            if (filename.startsWith(reference.getFrom()))
                newFilename = filename.replace(reference.getFrom(), reference.getTo());
        }

        // retorna o nome do arquivo
        return newFilename;
    }

    // retorna o nome do objeto
    private String getFilenameFromURL(URL url) throws Exception {

        // lista de valores para montar o nome do arquivo
        ArrayList<String> values = new ArrayList<String>();

        // valida o inicio da query string, para determinar o tipo
        // quando o inicio for ?WSDL, o arquivo eh tipo wsdl, definido no path
        // quando o inicio for ?SCHEMA, o arquivo eh tipo xsd, definido no querystring
        // quando o inicio for ?RESOURCE=, o nome do arquivo ja eh xsd, definido no querystring
        if (url.getQuery().toUpperCase().startsWith("WSDL")) {

            // obtem do path o nome do arquivo, retirando qualquer extensao existente
            // exemplo: /OCSMTAuthenticationCenter/AuthenticationCenter2WS.asmx
            // retorno: array com OCSMTAuthenticationCenter, AuthenticationCenter2WS
            values.addAll(Arrays.asList(URLDecoder.decode(url.getPath(), "UTF-8").split("\\.")[0].split("/")));

            // remove o primeiro indice, pois eh vazio
            if (values.get(0).equals(""))
                values.remove(0);

            // adiciona a extensao wsdl no ultimo nome
            values.set(values.size() - 1, values.get(values.size() - 1) + ".wsdl");

            // retorno final do array
            // OCSMTAuthenticationCenter, AuthenticationCenter2WS.wsdl

        } else if (url.getQuery().toUpperCase().startsWith("SCHEMA")) {

            // obtem da querystring o nome do arquivo
            // exemplo: SCHEMA/ModeloCanonico/XSD/Geral/CabecalhoVivo
            // retorno: array com SCHEMA, ModeloCanonico, XSD, Geral, CabecalhoVivo
            values.addAll(Arrays.asList(URLDecoder.decode(url.getQuery(), "UTF-8").split("/")));

            // remove o valor SCHEMA, pois nao eh necessario
            values.remove(0);

            // adiciona a extensao xsd no ultimo nome
            values.set(values.size() - 1, values.get(values.size() - 1) + ".xsd");

            // retorno final do array
            // ModeloCanonico, XSD, Geral, CabecalhoVivo.xsd

        } else if (url.getQuery().toUpperCase().startsWith("RESOURCE=")) {

            // obtem da querystring o arquivo e para melhor localizacao, o caminho do path
            // --este caso eh para o soa suite
            // Exemplo:
            //			path: soa-infra/services/desconto/RegistrarDesconto/Service
            //			querystring: RESOURCE=/schemas/ParametrosRegistrarDesconto.xsd
            // retorno: array com schemas, ParametrosRegistrarDesconto.xsd
            //
            // existe o caso do RESOURCE comecar com RESOURCE=oramds:/apps/...
            // neste caso, retiro oramds

            // split por = , para ignorar a palavra "RESOURCE"
            values.addAll(Arrays.asList(URLDecoder.decode(url.getQuery(), "UTF-8").split("=")[1].split("/")));

            // primeiro indice como oramds, remove
            if (values.get(0).equals("oramds:"))
                values.remove(0);

            // primeiro indice vazio, remove
            if (values.get(0).equals(""))
                values.remove(0);

        } else {

            // nao eh possivel definir o nome, lanca exception
            throw new Exception("N�o foi possivel definir o nome do arquivo atrav�s da URL: " + url.toString());
        }

        // adiciona o host no caminho
        values.add(0, url.getHost());

        // transforma a List em array para o join
        String[] array = new String[values.size()];
        values.toArray(array);

        // retorna o nome
        return getFilenameReference(StringUtils.join(array, "/"));
    }

    // utilizado para realizar qualquer modificacao no document, antes de processar o mesmo
    private URL normalizeResource(URL remote, Document document) throws Exception {

        // xpaths
        String xPathTranslate = "translate(local-name(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')";
        String xPathAbstractWSDL = "//*[" + xPathTranslate + "='abstractwsdl']";
        String xPathServiceWSDLLocation = "//*[" + xPathTranslate + "='service']/@*[" + xPathTranslate + "='wsdllocation']";

        // define a nova url
        URL newURL = remote;

        // procura pela tag <AbstractWSDL>
        // caso tenha esta tag eh proveniente do soa suite
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xpath.evaluate(xPathAbstractWSDL, document, XPathConstants.NODE);

        // caso encontre, o tratamento deste documento sera diferente
        if (node != null) {

            // url root
            String rootUrl = node.getTextContent().split("!\\d+\\.\\d+")[0];

            // url de pesquisas
            String resourceUrl = rootUrl.replace("services", "directWSDL") + "/Service?resource=";

            // monta a url para obter o composite.xml
            URL compositeUrl = new URL(resourceUrl + "/composite.xml");
            Document composite = XMLUtils.getRemoteXml(compositeUrl);

            // procura pela localizacao real do servico no composite
            String wsdlLocation = (String) xpath.evaluate(xPathServiceWSDLLocation, composite, XPathConstants.STRING);

            // obtem o document real com todos os wsdlLocation corretos
            URL metadataUrl = new URL(resourceUrl + wsdlLocation);
            Document metadata = XMLUtils.getRemoteXml(metadataUrl);

            // url real do documento
            newURL = metadataUrl;

            // busca todos imports e includes do document
            NodeList list = (NodeList) XMLUtils.getNodeListExternalResource(document);

            // realiza a substituicao de acordo com o metadado
            for (int index = 0; index < list.getLength(); index++) {

                // obtem o elemento
                Element element = (Element) list.item(index);

                // valida se possui schemaLocation
                if (element.hasAttribute("schemaLocation") && !element.getAttribute("schemaLocation").equals("")) {

                    // define o novo schemaLocation como o atual
                    String newSchemaLocation = element.getAttribute("schemaLocation");

                    // transforma o schemaLocation em URL
                    URL schemaLocation = new URL(element.getAttribute("schemaLocation"));

                    // pesquisa do arquivo
                    String search = "";

                    // nao existindo querystring, o arquivo esta no path
                    if (schemaLocation.getQuery() == null || schemaLocation.getQuery().equals(""))
                        search = URLDecoder.decode(schemaLocation.toString(), "UTF-8").replace(rootUrl + "/", "");

                        // neste caso, o arquivo esta especificado na querystring
                    else if (schemaLocation.getQuery().startsWith("XSD="))
                        search = URLDecoder.decode(schemaLocation.getQuery(), "UTF-8").split("=")[1].replaceAll("\\.\\./", "");

                    // valida se foi informado pesquisa
                    if (!search.equals("")) {

                        // xpath para validar se o schemaLocation existe no metadado
                        String xPathExists = "(//*[local-name()='include' and namespace-uri()='http://www.w3.org/2001/XMLSchema']|" +
                                "//*[local-name()='import' and namespace-uri()='http://www.w3.org/2001/XMLSchema'])" +
                                "/@*[local-name()='schemaLocation']" +
                                "/self::node()[contains(self::node(), '" + search + "') or " +
                                "contains(self::node(), '" + URLEncoder.encode(search, "UTF-8") + "')]";

                        // procura pelo arquivo do metadados
                        String schemaLocationMetadata = (String) xpath.evaluate(xPathExists, metadata, XPathConstants.STRING);

                        // caso tenha encontrado, atualiza o schemaLocation do document
                        if (!"".equals(schemaLocationMetadata))
                            newSchemaLocation = schemaLocationMetadata;
                    }

                    // atualiza para o novo schemaLocation
                    element.setAttribute("schemaLocation", newSchemaLocation);
                }
            }
        }

        // retorno da nova URL
        return newURL;
    }

    // obtem o documento remoto e todos documentos associado ao mesmo
    private Hashtable<String, Document> getRemoteResources(URL remote, List<String> processed) throws Exception {

        // valida se o parametro de processados eh nulo
        if (processed == null)
            processed = new ArrayList<String>();

        // hash de documentos
        Hashtable<String, Document> documents = new Hashtable<String, Document>();

        // lista de urls do documento
        ArrayList<URL> urls = new ArrayList<URL>();

        // valida a exibicao no log
        if (verbose)
            log.info("Download do documento na URL: " + URLDecoder.decode(remote.toString(), "UTF-8") + " ...");

        // obtem o documento
        Document document = XMLUtils.getRemoteXml(remote);

        // realiza modificacoes necessarias no document
        // e retorna uma nova url quando for necessario
        remote = normalizeResource(remote, document);

        // nome do arquivo e o documento
        String filename = getFilenameFromURL(remote);

        // adiciona no hash de documentos e na lista de processados
        documents.put(filename, document);
        processed.add(filename);

        // procura por todas urls dentro do documento
        urls.addAll(XMLUtils.getURLExternalResource(document));

        // processamento das urls
        for (URL url : urls)
            // evita referencia ciclica
            if (!processed.contains(getFilenameFromURL(url)))
                documents.putAll(getRemoteResources(url, processed));

        // retorno do hash apenas com objetos novos
        return documents;
    }

    // obtem o wsdl e todos xsds associados aos mesmo
    public void saveWSDLResources(URL remote, String packageWsdl, File dirXslt, List<String> processed) throws Exception {

        // quantidade de itens
        long count = 0;

        // valida se o protocolo da url eh http ou https
        if (!CommonUtils.isInList(remote.getProtocol(), "http", "https"))
            throw new Exception("Invalid protocol on " + remote.toString());

        // inicio do processo
        Elapsed elapsed = Elapsed.init();

        // log
        log.info("Processando URL: " + remote.toString() + " ...");

        // processa todos os documentos
        Hashtable<String, Document> documents = getRemoteResources(remote, processed);

        // salva a quantidade de itens que foram feito download
        count = documents.size();

        // para todos os documentos encontrados, salva no disco
        while (documents.keys().hasMoreElements()) {

            // xslt
            File xslt = null;

            // obtem o proximo arquivo
            String filename = documents.keys().nextElement();

            // arquivo de destino
            File file = new File(dirOutput, filename);

            // arquivo xslt
            if (dirXslt != null) {

                // obtem o nome do arquivo sem extensao e adiciona xsl
                String extension = FileUtils.getFileExtension(file);

                // modifica a extensao para .xsl
                xslt = new File(dirXslt, filename.replaceAll("." + extension, ".xsl"));

                // caso o arquivo nao exista, reatribui null
                if (!xslt.exists())
                    xslt = null;
            }

            // cria os diretorios se necessario
            file.getParentFile().mkdirs();

            // define o diretorio root
            String rootPath = (new String(new char[filename.split("/").length - 1])).replace("\0", "../");

            // obtem o document, para atualizacoes
            Document document = documents.get(filename);

            // atualiza a referencia dentro do document
            NodeList external = XMLUtils.getNodeListExternalResource(document);
            for (int index = 0; index < external.getLength(); index++) {

                Element element = (Element) external.item(index);
                if (element.hasAttribute("schemaLocation")) {
                    String newSchemaLocation = rootPath + getFilenameFromURL(new URL(element.getAttribute("schemaLocation")));
                    element.setAttribute("schemaLocation", newSchemaLocation);
                }
            }

            // salva o arquivo no sistema operacional
            XMLUtils.saveXml(document, file, xslt);

            // remove o documento do hash, para liberar memoria
            documents.remove(filename);
        }

        // fim do processo
        String time = elapsed.toString();
        log.info("Itens: " + count + " / Tempo: " + time);
        log.info("\n");
    }
}