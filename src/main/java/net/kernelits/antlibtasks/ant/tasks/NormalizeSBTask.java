package net.kernelits.antlibtasks.ant.tasks;

import net.kernelits.antlibtasks.utils.FileUtils;
import net.kernelits.antlibtasks.utils.XMLUtils;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Task para normalizar o local dos arquivos de servicos, criando uma
 * estrutura baseado no targetNamespace de cada arquivos
 * e ajusta a referencia
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class NormalizeSBTask extends Task {

    private File dirSearch = null;
    private File dirOutput = null;
    private boolean excProcessedFiles = true;

    // adiciona o diretorio de pesquisa
    public void setSearch(File value) {
        dirSearch = value;
    }

    // adiciona o diretorio de extracao
    public void setOutput(File value) {
        dirOutput = value;
    }

    // verifica a exclusao de arquivos ja processados
    public void setExcludeProcessedFiles(boolean value) {
        excProcessedFiles = value;
    }


    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {

        // realiza a validacao dos parametros
        validate();

        try {

            // exclui o diretorio de saida
            FileUtils.delete(dirOutput);

            // recria o diretorio
            dirOutput.mkdirs();

            // processo de extracao de arquivos
            // no diretorio de pesquisa, procura
            // arquivos do tipo jar, zip, rar, xsd e wsdl
            // para normalizacao
            File[] files = FileUtils.list(dirSearch, new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar") ||
                            pathname.getName().endsWith(".zip") ||
                            pathname.getName().endsWith(".rar") ||
                            pathname.getName().endsWith(".xsd") ||
                            pathname.getName().endsWith(".wsdl");
                }
            });

            // loop dos arquivos encontrados
            for (File file : files) {

                // obtem o nome para verificacao
                String filename = file.getName();

                if (filename.endsWith(".jar") ||
                        filename.endsWith(".zip") ||
                        filename.endsWith(".rar"))

                    // processa arquivos compactados
                    processCompressedFile(file);

                else

                    // processa arquivos finais
                    processXmlFile(file, dirOutput);

                // exclui arquivos processados
                if (excProcessedFiles)
                    FileUtils.delete(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new BuildException(ex);
        }
    }

    public void validate() throws BuildException {

        // realiza as validacoes nos parametros
        if (dirSearch == null)
            throw new BuildException("Parametro search nao informado");
        if (dirOutput == null)
            throw new BuildException("Parametro output nao informado");
    }

    private void processCompressedFile(File zip) throws Exception {

        // cria o destino da extracao do arquivo
        File dest = new File(zip.getAbsoluteFile() + "_ext");

        // descompacta o arquivo
        FileUtils.unzip(zip, dest, true);

        // list os arquivos extraidos
        File[] files = FileUtils.list(dest, new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar") ||
                        pathname.getName().endsWith(".zip") ||
                        pathname.getName().endsWith(".rar") ||
                        pathname.getName().endsWith(".xsd") ||
                        pathname.getName().endsWith(".wsdl");
            }
        });

        // loop dos arquivos encontrados
        for (File file : files) {

            // obtem o nome para verificacao
            String filename = file.getName();

            if (filename.endsWith(".jar") ||
                    filename.endsWith(".zip") ||
                    filename.endsWith(".rar"))

                // caso seja outro zip, chama recusirvamente esta funcao
                processCompressedFile(file);

            else

                // realiza o processamento dos arquivos de outra extensao
                processXmlFile(file, dirOutput);
        }

        if (excProcessedFiles)
            FileUtils.delete(dest);
    }

    private void processXmlFile(File xml, File dest) throws Exception {

        // obtem o documento
        Document document = XMLUtils.parseXml(xml);

        // busca o no root do documento
        Node rootNode = XMLUtils.getXmlRootNode(document);

        // obtem o targetNamespace para o novo local do arquivo
        String targetNamespace = XMLUtils.getTargetNamespace(rootNode);
        File newDest = new File(dest, targetNamespace);

        // define o novo nome do arquivo, caso seja necessario
        File newFilename = getNewFilename(xml);

        // atualiza os caminhos dentro do arquivo
        updateLocations(document, targetNamespace, xml.getParentFile());

        // cria o novo local do arquivo
        newDest.mkdirs();

        // grava modificacoes no arquivo ja no novo diretorio
        XMLUtils.saveXml(document, new File(newDest, newFilename.getName()));
    }

    private void updateLocations(Document document, String rootTargetNamespace, File source) throws URISyntaxException, MalformedURLException, XPathExpressionException {

        // define o nivel de navegacao pelos arquivos baseado no rootTargetNamespace
        String rootPath = "";
        if (!rootTargetNamespace.equals(""))
            // obtem a quantidade de separadores para repetir quantos ../ devera ter
            rootPath = (new String(new char[rootTargetNamespace.split("/").length]).replace("\0", "../"));

        // ajusta os paths
        NodeList list = XMLUtils.getNodeListExternalResource(document);
        for (int index = 0; index < list.getLength(); index++) {

            // obtem o elemento encontrado
            Element element = (Element) list.item(index);

            // define novo arquivo somente se existe schemaLocation
            if (element.hasAttribute("schemaLocation")) {

                // novo nome de arquivo
                File newFile = getNewFilename(new File(source, new File(element.getAttribute("schemaLocation")).getName()));

                // novo local do schema
                String schemaLocation = newFile.getName();
                String newSchemaLocation = schemaLocation;

                // obtem o targetNamespace do parent
                String targetNamespace = XMLUtils.getTargetNamespace(element.getParentNode());

                // verifica se o filho targetNamespace eh diferente do targetNamespace do documento
                if (!rootTargetNamespace.equalsIgnoreCase(targetNamespace)) {

                    // verifica se o schemaLocation nao foi modificado
                    if (!schemaLocation.contains(rootPath + targetNamespace))
                        newSchemaLocation = rootPath + targetNamespace + "/" + schemaLocation;
                }

                // atribui a nova localizacao do arquivo
                element.setAttribute("schemaLocation", newSchemaLocation);
            }
        }
    }

    // define o novo nome do arquivo para o output
    private File getNewFilename(File file) {

        // nome do arquivo
        File newFile = null;

        // transforma o nome do arquivo em string
        String filename = file.getName();

        // obtem a extensao do arquivo
        String fileExtension = FileUtils.getFileExtension(file);

        // nome do arquivo sem extensao
        String fileWithoutExtension = filename.replaceAll("." + fileExtension, "");

        // pega o ultimo caracter da string
        String lastString = fileWithoutExtension.substring(fileWithoutExtension.length() - 1);

        // verifica se o ultimo byte do nome eh numero
        if (lastString.matches("[0-9]")) {

            // ultimo byte eh numero ?

            // cria um novo nome do arquivo
            File fileExists = new File(file.getParent(), fileWithoutExtension.substring(0, fileWithoutExtension.length() - 1) + "." + fileExtension);

            // verifica se o arquivo existe
            if (fileExists.exists()) {

                // chama a mesma funcao recursiva
                newFile = getNewFilename(fileExists);

            } else {

                // o arquivo nao existe removendo o ultimo numero, entao o nome do arquivo eh real e nao sequencia
                newFile = file;
            }
        } else {

            // ultimo byte do arquivo nao eh numero, entao eh o mesmo nome
            newFile = file;
        }

        // retorna o nome do novo nome do arquivo
        return newFile;
    }
}