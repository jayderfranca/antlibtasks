package net.kernelits.antlibtasks.ant.conditions;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;

/**
 * Condicao para verificar se uma determinada string inicia com
 * o valor especificacado
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class StartWith extends ConditionBase implements Condition {

    private String text = null;
    private String value = null;
    private boolean casesensitive = true;

    /**
     * @param texto a ser avaliado
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * @param valor da condicao
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @param indica se a comparacao ira considerar diferenca de maiusculos e minusculos
     */
    public void setCasesensitive(boolean value) {
        this.casesensitive = value;
    }

    /* (non-Javadoc)
     * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
     */
    public boolean eval() throws BuildException {

        // valida os valores informados
        if (text == null)
            throw new BuildException("A propriedade text nao foi informada");
        if (value == null)
            throw new BuildException("A propriedade value nao foi informada");

        try {
            // verifica se o texto inicia com o valor especificado
            if (this.casesensitive)
                return this.text.startsWith(value);
            else
                return this.text.toLowerCase().startsWith(value.toLowerCase());
        } catch (Exception ex) {
            throw new BuildException(ex.getMessage(), ex);
        }
    }
}
