package net.kernelits.antlibtasks.ant.commands.replace;

import net.kernelits.antlibtasks.ant.commands.Command;
import org.apache.tools.ant.BuildException;

import java.util.regex.Pattern;

/**
 * Task de substituicao de strings
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class String extends Command {

    // propriedades
    private java.lang.String string = null;
    private java.lang.String oldString = null;
    private java.lang.String newString = null;
    private boolean all = false;
    private java.lang.String property = null;

    // sets
    public void setString(java.lang.String value) {
        string = value;
    }

    public void setOld(java.lang.String value) {
        oldString = value;
    }

    public void setNew(java.lang.String value) {
        newString = value;
    }

    public void setAll(boolean value) {
        all = value;
    }

    public void setProperty(java.lang.String value) {
        property = value;
    }

    @Override
    public void execute() throws BuildException {

        // variaveis
        java.lang.String value = "";

        // validacoes
        validate();

        // obtem o valor
        if (string.startsWith("${") && string.endsWith("}"))
            value = getProperty(string.substring(2, string.length() - 1));
        else
            value = string;

        // verifica se eh para substituir todas ocorrencias
        if (all)
            // nova string
            value = string.replaceAll(Pattern.quote(oldString), newString);
        else
            // nova string
            value = string.replaceFirst(Pattern.quote(oldString), newString);

        // verifica se foi informado que sera retornado em um parametro
        if (property != null && !property.equals(""))
            // cria a propriedade com o novo valor
            setProperty(property, value);
    }

    public void validate() throws BuildException {

        // realiza as validacoes nos parametros
        if (string == null)
            buildException("Parametro string nao informado");
        if (oldString == null)
            buildException("Parametro old nao informado");
        if (newString == null)
            buildException("Parametro new nao informado");
    }
}
