package edu.boun.edgecloudsim.core;

import edu.boun.edgecloudsim.utils.SimLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;

public class SimSettingsDsp extends SimSettings{

    protected double[][] applicationLookUpTable = null;
    protected ArrayList<double[]> applicationTaskLookUpTable = null;

    protected String[] applicationNames = null;
    protected ArrayList<String>[] applicationTaskNames = null;
    public static SimSettings getInstance(){
        if(instance == null) {
            instance = new SimSettingsDsp();
        }
        return instance;
    }

    public boolean initialize(String propertiesFile, String edgeDevicesFile, String applicationsFile, String topologyFile){

        boolean result = tryParseProperties(propertiesFile);
        parseApplicationsXML(applicationsFile);
        parseEdgeDevicesXML(edgeDevicesFile);
        return false;
    }

    @Override
    protected void parseApplicationsXML(String filePath)
    {
        Document doc = null;
        try {
            File devicesFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(devicesFile);
            doc.getDocumentElement().normalize();

            String mandatoryApplicationAttributes[] = {
                    "usage_percentage", //usage percentage [0-100]
                    "prob_cloud_selection", //prob. of selecting cloud [0-100]
                    "poisson_interarrival", //poisson mean (sec)
                    "active_period", //active period (sec)
                    "idle_period", //idle period (sec)
                    "delay_sensitivity"}; //delay_sensitivity [0-1]

            String optionalApplicationAttributes[] = {
                    "max_delay_requirement"}; //maximum delay requirement (sec)

            String mandatoryTaskAttributes[] = {
                    "data_upload", //avg data upload (KB)
                    "data_download", //avg data download (KB)
                    "task_length", //avg task length (MI)
                    "required_core", //required # of core
                    "vm_utilization_on_edge", //vm utilization on edge vm [0-100]
                    "vm_utilization_on_cloud", //vm utilization on cloud vm [0-100]
                    "vm_utilization_on_mobile", //vm utilization on mobile vm [0-100]
            };

            String optionalTaskAttributes[] = {

            };

            NodeList appList = doc.getElementsByTagName("application");
            applicationLookUpTable = new double[appList.getLength()][mandatoryApplicationAttributes.length + optionalApplicationAttributes.length];
            applicationTaskLookUpTable = new ArrayList<>();


            applicationNames = new String[appList.getLength()];
            applicationTaskNames = new ArrayList[appList.getLength()];

            for (int i = 0; i < appList.getLength(); i++) {
                Node appNode = appList.item(i);

                Element appElement = (Element) appNode;
                isAttributePresent(appElement, "name");
                String appName = appElement.getAttribute("name");
                applicationNames[i] = appName;

                for(int m=0; m<mandatoryApplicationAttributes.length; m++){
                    isElementPresent(appElement, mandatoryApplicationAttributes[m]);
                    applicationLookUpTable[i][m] = Double.parseDouble(appElement.
                            getElementsByTagName(mandatoryApplicationAttributes[m]).item(0).getTextContent());
                }

                for(int o=0; o<optionalApplicationAttributes.length; o++){
                    double value = 0;
                    if(checkElement(appElement, optionalApplicationAttributes[o]))
                        value =  Double.parseDouble(appElement.
                                getElementsByTagName(optionalApplicationAttributes[o]).item(0).getTextContent());

                    applicationLookUpTable[i][mandatoryApplicationAttributes.length + o] = value;
                }


                NodeList taskList = appElement.getElementsByTagName("task");

                double[] taskLookUp = new double[mandatoryTaskAttributes.length + optionalTaskAttributes.length];
                applicationTaskNames[i] = new ArrayList<>();
                for(int t=0; t<mandatoryTaskAttributes.length; t++){

                    Node taskNode = taskList.item(i);
                    Element taskElement = (Element) taskNode;
                    isAttributePresent(taskElement, "name");
                    String taskName = taskElement.getAttribute("name");
                    applicationTaskNames[i].add(taskName);


                    for(int m=0; m<mandatoryTaskAttributes.length; m++){
                        isElementPresent(taskElement, mandatoryTaskAttributes[m]);
                        taskLookUp[m] = Double.parseDouble(taskElement.
                                getElementsByTagName(mandatoryTaskAttributes[m]).item(0).getTextContent());
                    }

                    for(int o=0; o<optionalTaskAttributes.length; o++){
                        double value = 0;
                        if(checkElement(taskElement, optionalTaskAttributes[o]))
                            value =  Double.parseDouble(taskElement.
                                    getElementsByTagName(optionalTaskAttributes[o]).item(0).getTextContent());

                        taskLookUp[mandatoryTaskAttributes.length + o] = value;
                    }
                }
                applicationTaskLookUpTable.add(taskLookUp);

            }
        } catch (Exception e) {
            SimLogger.printLine("Edge Devices XML cannot be parsed! Terminating simulation...");
            e.printStackTrace();
            System.exit(1);
        }
    }


}
