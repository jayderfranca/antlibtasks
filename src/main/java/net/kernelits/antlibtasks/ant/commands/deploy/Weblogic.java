package net.kernelits.antlibtasks.ant.commands.deploy;

import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.ant.tasks.DeployTask;
import net.kernelits.antlibtasks.Constants;
import net.kernelits.antlibtasks.deploy.WLDeploy;
import net.kernelits.antlibtasks.os.IOperationalSystem;
import net.kernelits.antlibtasks.os.OSConnectParameters;
import net.kernelits.antlibtasks.os.impl.OSLinux;
import net.kernelits.antlibtasks.os.impl.OSWindows;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Task de deploy em servidores Weblogic
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class Weblogic extends Command {

    // propriedades
    private String hosts = null;
    private File file = null;
    private String context = null;
    private boolean print = false;

    // propriedades requeridas e opcionais no arquivo
    private Hashtable<String, String[]> required = new Hashtable<String, String[]>();
    private Hashtable<String, String[]> optional = new Hashtable<String, String[]>();

    // hash de deploys
    private Hashtable<String, Properties> deploys = new Hashtable<String, Properties>();

    public Weblogic() {

        super();

        // define as propriedades requeridas

        // servico = weblogic
        required.put(Constants.SERVICE, new String[]{Constants.WEBLOGIC});

        // nome da maquina
        required.put(Constants.MACHINE_NAME, new String[]{});

        // url de administracao
        required.put(Constants.APP_URL, new String[]{});

        // usuario da url de administracao
        required.put(Constants.APP_USER, new String[]{});

        // senha do usuario da url de administracao
        required.put(Constants.APP_PWD, new String[]{});

        // tipo do sistema operacional
        required.put(Constants.OS_TYPE, new String[]{Constants.WINDOWS, Constants.UNIX});

        // diretorio de deploy
        required.put(Constants.DEPLOY_DIR, new String[]{});

        // usuario do sistema operacional
        required.put(Constants.OS_USER, new String[]{});

        // define as propriedades opcionais

        // targets do weblogic
        optional.put(Constants.APP_TARGETS, new String[]{});

        // arquivo com a chave publica
        optional.put(Constants.PUBLIC_KEY, new String[]{});

        // dominio do usuario
        optional.put(Constants.OS_DOMAIN, new String[]{});

        // senha do usuario do sistema operacional
        optional.put(Constants.OS_PWD, new String[]{});
    }

    // sets
    public void setHosts(String value) {
        hosts = value;
    }

    public void setContext(String value) {
        context = value;
    }

    public void setFile(File value) {
        file = value;
    }

    public void setPrint(boolean value) {
        print = value;
    }

    @Override
    public void execute() throws BuildException {

        // declaracoes
        String machineName = "";
        String appUrl = "";
        String appTargets = "";
        String appUsername = "";
        String appPassword = "";
        String osType = "";
        String osDirDeploy = "";
        File osPubKey = null;
        String osDomain = "";
        String osUsername = "";
        String osPassword = "";
        String contextName = "";
        File jvm = null;
        Reference classpathRefId = null;
        List<Path> classpath = null;
        int ret = 0;
        WLDeploy deploy = null;
        IOperationalSystem os = null;
        OSConnectParameters osParameters = null;

        // validacoes
        validate();

        // varre os hosts de deploy para obter as propriedades e validar
        for (String host : hosts.split(",", -1)) {

            // cria a variavel de propriedades
            Properties props = getProperties(host);

            // valida as propriedades
            validateProperties(props);

            // adiciona no hash para deploy
            deploys.put(host, props);
        }

        // executa o deploy do arquivo informado nos hosts
        for (Iterator<String> it = deploys.keySet().iterator(); it.hasNext(); ) {

            // obtem o host que sera realizado o deploy
            String key = it.next();

            // obtem as variaveis
            machineName = deploys.get(key).getProperty(key + "." + Constants.MACHINE_NAME).toString();
            appUrl = deploys.get(key).getProperty(key + "." + Constants.APP_URL).toString();
            appUsername = deploys.get(key).getProperty(key + "." + Constants.APP_USER).toString();
            appPassword = deploys.get(key).getProperty(key + "." + Constants.APP_PWD).toString();
            osType = deploys.get(key).getProperty(key + "." + Constants.OS_TYPE).toString();
            osDirDeploy = deploys.get(key).getProperty(key + "." + Constants.DEPLOY_DIR).toString();
            osUsername = deploys.get(key).getProperty(key + "." + Constants.OS_USER).toString();

            // valida as propriedades de acordo com o tipo do sistema operacional
            if (osType.equals(Constants.WINDOWS)) {

                // dominio
                if (deploys.get(key).containsKey(key + "." + Constants.OS_DOMAIN))
                    osDomain = deploys.get(key).getProperty(key + "." + Constants.OS_DOMAIN).toString();
                else
                    buildException("Sistema operacional tipo Windows necessita do dominio informado");

                // senha
                if (deploys.get(key).containsKey(key + "." + Constants.OS_PWD))
                    osPassword = deploys.get(key).getProperty(key + "." + Constants.OS_PWD).toString();

            } else {

                // chave publica
                if (deploys.get(key).containsKey(key + "." + Constants.PUBLIC_KEY)) {

                    // obtem a chave publica
                    osPubKey = new File(deploys.get(key).getProperty(key + "." + Constants.PUBLIC_KEY).toString());

                    // verifica se a chave publica existe
                    if (!osPubKey.exists())
                        buildException("Chave Publica invalida: " + osPubKey.getAbsolutePath());
                }

                // senha
                if (deploys.get(key).containsKey(key + "." + Constants.OS_PWD))
                    osPassword = deploys.get(key).getProperty(key + "." + Constants.OS_PWD).toString();

            }

            contextName = context;
            jvm = ((DeployTask) getTask()).getJvm();
            classpathRefId = ((DeployTask) getTask()).getClasspathRefId();
            classpath = ((DeployTask) getTask()).getClasspath();

            // verifica se foi informado os targets
            if (deploys.get(key).containsKey(key + "." + Constants.APP_TARGETS))
                appTargets = deploys.get(key).getProperty(key + "." + Constants.APP_TARGETS).toString();

            // verifica se a jvm foi informada e existe
            // caso informada e nao existe, lanca exception
            if (jvm != null && !jvm.exists())
                buildException("JVM nao encontrada");

            // cria o comando de deploy
            try {
                deploy = new WLDeploy(getProject(), getTask(), appUrl, appTargets,
                        appUsername, appPassword, contextName, jvm, classpathRefId, classpath, print);
            } catch (Exception ex) {
                buildException("Erro na execucao do comando", ex);
            }

            // cria o comando de copia
            try {

                // cria a classe do sistema operacional informado
                if (osType.equals(Constants.WINDOWS)) {

                    // cria o objeto da classe
                    os = new OSWindows();

                    // cria os parametros da classe
                    osParameters = new OSConnectParameters();
                    osParameters.hostname = machineName;
                    osParameters.domain = osDomain;
                    osParameters.username = osUsername;
                    osParameters.password = osPassword;

                } else {

                    // cria o objeto da classe
                    os = new OSLinux();

                    // cria os parametros da classe
                    osParameters = new OSConnectParameters();
                    osParameters.hostname = machineName;
                    osParameters.port = 22;
                    osParameters.username = osUsername;
                    if (osPubKey != null)
                        osParameters.publicKey = osPubKey;
                    else if (OSLinux.class.getResourceAsStream("/br/com/sysmap/ant/ssh.key") != null)
                        osParameters.streamPublicKey = OSLinux.class.getResourceAsStream("/br/com/sysmap/ant/ssh.key");
                    osParameters.password = osPassword;
                }

            } catch (Exception ex) {
                buildException("Erro na execucao do comando", ex);
            }

            // inicia a operacao de deploy no servidor
            log.info("Deploy do arquivo " + file.getName() + " na maquina " + machineName);

            // verifica se foi informado os targets
            if (!"".equals(appTargets))
                log.info("Targets: " + appTargets);

            // linha em branco
            log.info("");

            // para o contexto para retirada do contexto
            log.info("Parando o contexto " + contextName + " ...");
            ret = deploy.stop(64);
            if (ret != 0) buildException("Erro na execucao do comando");
            log.info("");

            // realiza o undeploy do contexto para a entrada do contexto com o novo arquivo
            log.info("Retirando o contexto " + contextName + " ...");
            ret = deploy.undeploy(64);
            if (ret != 0) buildException("Erro na execucao do comando");
            log.info("");

            // copia o arquivo para o destino
            log.info("Copiando arquivo " + file.getName() + " para maquina " + machineName);
            try {

                // conecta com a maquina remota
                os.connect(osParameters);

                // realiza a copia
                os.copy(file, osDirDeploy + file.getName());

                // desconecta da maquina remota
                os.disconnect();

            } catch (Exception e) {
                buildException("Erro na execucao do comando", e);
            }

            // realiza o deploy do contexto com o novo arquivo
            // em caso de falha devera lancar erro
            log.info("Criando o contexto " + contextName + " com o novo arquivo ...");
            ret = deploy.deploy(true, 64, osDirDeploy + file.getName());
            if (ret != 0) buildException("Erro na execucao do comando");
            log.info("");

            // inicia o contexto com o novo deploy
            // em caso de falha devera lancar erro
            log.info("Iniciando o contexto " + contextName + " ...");
            ret = deploy.start(true, 64);
            if (ret != 0) buildException("Erro na execucao do comando");
            log.info("");
        }
    }

    public void validate() throws BuildException {

        // realiza as validacoes nos parametros
        if (hosts == null || "".equalsIgnoreCase(hosts.trim()))
            buildException("Parametro hosts nao informado");

        if (context == null || "".equalsIgnoreCase(context.trim()))
            buildException("Parametro context nao informado");

        if (file == null || "".equalsIgnoreCase(file.getName()))
            buildException("Parametro file nao informado");
        else if (!file.exists())
            buildException("Arquivo no parametro file nao encontrado");
    }

    private Properties getProperties(String host) throws BuildException {

        // carrega as propriedades para uma variavel de propriedades separada
        Properties props = new Properties();

        // cria o nome do host
        props.put("host.name", host);

        // carrega as demais propriedades (requeridas)
        for (Iterator<String> it = required.keySet().iterator(); it.hasNext(); ) {
            String prop = it.next();
            if (getProject().getProperties().containsKey(host + "." + prop))
                props.put(host + "." + prop, getProject().getProperties().get(host + "." + prop).toString());
        }

        // carrega as demais propriedades (opcionais)
        for (Iterator<String> it = optional.keySet().iterator(); it.hasNext(); ) {
            String prop = it.next();
            if (getProject().getProperties().containsKey(host + "." + prop))
                props.put(host + "." + prop, getProject().getProperties().get(host + "." + prop).toString());
        }

        // retorna os novos valores obtidos
        return props;
    }

    public void validateProperties(Properties props) throws BuildException {

        // obtem o host que esta sendo tratado
        String host = props.getProperty("host.name");

        // varre a lista de propriedades requeridas
        for (Iterator<String> it = required.keySet().iterator(); it.hasNext(); ) {

            // obtem o nome da propriedade
            String prop = it.next();

            // verifica se a propriedade requerida esta vazia
            if (!props.containsKey(host + "." + prop) ||
                    "".equals(((String) props.get(host + "." + prop)).trim()))
                buildException("Propriedade nao atribuida (" + host + "." + prop + ")");

            // obtem o valor da propriedade
            String propValue = ((String) props.get(host + "." + prop)).trim();

            // obtem a lista de valores permitidos
            String[] values = required.get(prop);

            // boolean para indicar que o valor nao se encontra na lista
            boolean isInList = false;

            // variavel da lista de valores
            String valueList = "";

            // varre a lista de valores permitidos
            if (values.length > 0)
                for (String value : values) {
                    valueList = valueList + ("".equals(valueList) ? value : ", " + value);
                    if (propValue.equalsIgnoreCase(value))
                        isInList = true;
                }
            else
                isInList = true;

            // caso o valor nao esteja na lista lanca a exception
            if (!isInList)
                buildException("Propriedade " + host + "." + prop + " possui um valor nao reconhecido. (Permitidos: " + valueList + ")");
        }
    }
}
