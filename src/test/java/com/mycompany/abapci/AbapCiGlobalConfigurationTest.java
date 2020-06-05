package com.mycompany.abapci;



import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.jvnet.hudson.test.RestartableJenkinsRule;

public class AbapCiGlobalConfigurationTest {

    @Rule
    public RestartableJenkinsRule rr = new RestartableJenkinsRule();

    /**
     * Tries to exercise enough code paths to catch common mistakes:
     * <ul>
     * <li>missing {@code load}
     * <li>missing {@code save}
     * <li>misnamed or absent getter/setter
     * <li>misnamed {@code textbox}
     * </ul>
     */

    @Test
        public void uiAndStorageSapServername() {
            final String testValue = "Hello Servername"; 

            rr.then(r -> {
            assertNull("not set initially", AbapCiGlobalConfiguration.get().getSapServername());
            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlTextInput textbox = config.getInputByName("_.sapServername");
            textbox.setText(testValue);
            r.submit(config);
            assertEquals("global config page let us edit it", testValue, AbapCiGlobalConfiguration.get().getSapServername());
        });
            
        rr.then(r -> {
            assertEquals("still there after restart of Jenkins", testValue, AbapCiGlobalConfiguration.get().getSapServername());
        });
    }

            @Test
        public void uiAndStorageSapPort() {
            final int testValue = 1234; 

            rr.then(r -> {
            Assert.assertEquals(0, AbapCiGlobalConfiguration.get().getSapPort());
            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlNumberInput textbox = config.getInputByName("_.sapPort");
            textbox.setText(Integer.toString(testValue));
            r.submit(config);
            assertEquals("global config page let us edit it", testValue, AbapCiGlobalConfiguration.get().getSapPort());
        });
            
        rr.then(r -> {
            assertEquals("still there after restart of Jenkins", testValue, AbapCiGlobalConfiguration.get().getSapPort());
        });
    }

}
