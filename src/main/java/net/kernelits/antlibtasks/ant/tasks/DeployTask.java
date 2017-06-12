package net.kernelits.antlibtasks.ant.tasks;

import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.ant.commands.deploy.JBoss;
import net.kernelits.antlibtasks.ant.commands.deploy.Weblogic;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Task para deploys em servidores
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class DeployTask extends Task {

    private Properties properties = new Properties();
    private List<Path> classpath = new ArrayList<Path>();
    private Reference classpathRefId;
    private File jvm;

    // sets
    public void setProperties(File value) throws FileNotFoundException, IOException {
        properties.load(new FileInputStream(value));
    }

    // adiciona task de jboss
    public void addJBoss(JBoss jboss) {
        addCommand(jboss);
    }

    // adiciona task de weblogic
    public void addWeblogic(Weblogic weblogic) {
        addCommand(weblogic);
    }

    // adiciona classpath
    public void addClasspath(Path value) {
        classpath.add(value);
    }

    public List<Path> getClasspath() {
        return classpath;
    }

    public void setClasspathRefId(Reference value) {
        classpathRefId = value;
    }

    public Reference getClasspathRefId() {
        return classpathRefId;
    }

    // define a JVM para comunicacao com o servidor
    public void setJvm(File value) {
        jvm = value;
    }

    public File getJvm() {
        return jvm;
    }

    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {

        // antes de executar qualquer task, carrega as propriedades no projeto
        for (Enumeration<Object> el = properties.keys(); el.hasMoreElements(); ) {
            String key = (String) el.nextElement();
            getProject().setProperty(key, properties.getProperty(key));
        }

        // executa todos os comandos adicionados
        for (Command command : getCommands()) {
            command.execute();
        }
    }
}
