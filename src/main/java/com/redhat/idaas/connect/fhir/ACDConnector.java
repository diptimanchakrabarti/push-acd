package com.redhat.idaas.connect.fhir;



import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.BasicAuthenticator;
import com.ibm.cloud.sdk.core.service.exception.ServiceResponseException;
import com.ibm.watson.health.acd.v1.AnnotatorForClinicalData;
import com.ibm.watson.health.acd.v1.model.Annotator;
import com.ibm.watson.health.acd.v1.model.Concept;
import com.ibm.watson.health.acd.v1.model.ContainerGroup;
import com.ibm.watson.health.acd.v1.model.Flow;
import com.ibm.watson.health.acd.v1.util.FlowUtil;
import io.connectedhealth_idaas.eventbuilder.events.platform.RoutingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import io.connectedhealth_idaas.eventbuilder.parsers.clinical.FHIRStreamParser;


import java.util.Arrays;
import java.util.List;


public class ACDConnector {

    public ACDConnector(){}

    @Autowired
    private ConfigProperties config;

    public String acdAnnotator(String msgBody) {

        FHIRStreamParser fhir=new FHIRStreamParser();
        RoutingEvent evnt=fhir.buildRoutingEvent(msgBody);
        String text=evnt.getMessageData();
        Authenticator authenticator = new BasicAuthenticator("apikey", config.getAcdAPI());
        AnnotatorForClinicalData acd = new AnnotatorForClinicalData(
                config.getAcdVersion(),
                AnnotatorForClinicalData.DEFAULT_SERVICE_NAME,
                authenticator);


        acd.setServiceUrl(config.getAcdURI());
        //String text = "Patient has lung cancer, but did not smoke. She may consider chemotherapy as part of a treatment plan.";


        try {

            List<String> annotators = Arrays.asList(
                    Annotator.Name.CONCEPT_DETECTION,
                    Annotator.Name.NEGATION);

            Flow flow = new FlowUtil.Builder()
                    .annotators(annotators).build();

            String retval="";
            int i=0;
            ContainerGroup response = acd.analyze(text, flow);

            List<Concept> concepts = response.getConcepts();

            for (Concept c : concepts) {
                if (i==0){
                    retval="Type: " + c.getType()
                            + " Name: " + c.getPreferredName()
                            + " | Negated (t/f): " + c.get("negated");
                } else {
                    retval=retval +","+ "Type: " + c.getType()
                            + " Name: " + c.getPreferredName()
                            + " | Negated (t/f): " + c.get("negated");
                }
                /*
                System.out.println("Type: " + c.getType()
                        + " Name: " + c.getPreferredName()
                        + " | Negated (t/f): " + c.get("negated"));
                        */

            }
            return retval;
        } catch (ServiceResponseException e) {
            return "Service returned status code: Error details: " + e.getStatusCode() + " " + e.getMessage() + " " + e.getDebuggingInfo().toString();

        }



    }


}
