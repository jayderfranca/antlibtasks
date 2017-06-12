package net.kernelits.antlibtasks.ant.types.sbget;

import org.apache.tools.ant.types.DataType;

import java.net.URL;

/**
 * Define um WSDL remoto que ser� processado pela task SBGet
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class Remote extends DataType {

    // variaveis
    private URL url = null;
    private String packageWsdl = null;

    // sets
    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getPackage() {
        return packageWsdl;
    }

    public void setPackage(String packageWsdl) {
        this.packageWsdl = packageWsdl;
    }
}
