package net.kernelits.antlibtasks.ant.types.sbget;

import org.apache.tools.ant.types.DataType;

/**
 * Define uma referencia que sera alterada na task SBGet
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class Reference extends DataType {

    // variaveis
    private String from = null;
    private String to = null;

    // sets
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

}
