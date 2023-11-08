/*
 * Title:        EdgeCloudSim - Main Application
 *
 * Description:  Main application for Simple App
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.sample_app6;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainApp {

    public static final int EXPECTED_NUM_OF_ARGS = 6;
    public static final String APP_NAME = "sample_app6";
    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {
        //disable console output of cloudsim library
        Log.disable();

        //enable console output and file output of this application
        SimLogger.enablePrintLog();

        String configFile = null;
        String outputFolder = null;
        String edgeDevicesFile = null;
        String applicationsFile = null;
        String topologyFile = null;

        int iterationNumber = 1;

        if (args.length == EXPECTED_NUM_OF_ARGS) {
            configFile = args[0];
            edgeDevicesFile = args[1];
            applicationsFile = args[2];
            topologyFile = args[3];
            outputFolder = args[4];
            iterationNumber = Integer.parseInt(args[5]);
        } else {
            SimLogger.printLine("Simulation setting file, output folder and iteration number are not provided! Using default ones...");
            configFile = "scripts/" + APP_NAME + "/config/default_config.properties";
            applicationsFile = "scripts/" + APP_NAME + "/config/applications.xml";
            edgeDevicesFile = "scripts/" + APP_NAME + "/config/edge_devices.xml";
            topologyFile = "scripts/" + APP_NAME + "/config/topology.graphml";
        }

        //load settings from configuration file
        SimSettings SS = SimSettings.getInstance();

        if (!SS.initialize(configFile, edgeDevicesFile, applicationsFile, topologyFile)) {
            SimLogger.printLine("cannot initialize simulation settings!");
            System.exit(0);
        }


        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date SimulationStartDate = Calendar.getInstance().getTime();
        String now = df.format(SimulationStartDate);
        SimLogger.printLine("Simulation started at " + now);
        SimLogger.printLine("----------------------------------------------------------------------");

        for (int ite = 0; ite < iterationNumber; ite++) {

            outputFolder = "sim_results/" + APP_NAME + "/ite" + iterationNumber;

            if (SS.getFileLoggingEnabled()) {
                SimLogger.enableFileLog();
                SimUtils.cleanOutputFolder(outputFolder);
            }

            for (int j = SS.getMinNumOfMobileDev(); j <= SS.getMaxNumOfMobileDev(); j += SS.getMobileDevCounterSize()) {
                for (int k = 0; k < SS.getSimulationScenarios().length; k++) {
                    for (int i = 0; i < SS.getOrchestratorPolicies().length; i++) {
                        String simScenario = SS.getSimulationScenarios()[k];
                        String orchestratorPolicy = SS.getOrchestratorPolicies()[i];
                        Date ScenarioStartDate = Calendar.getInstance().getTime();
                        now = df.format(ScenarioStartDate);

                        SimLogger.printLine("Scenario started at " + now);
                        SimLogger.printLine("Scenario: " + simScenario + " - Policy: " + orchestratorPolicy + " - #iteration: " + iterationNumber);
                        SimLogger.printLine("Duration: " + SS.getSimulationTime() / 3600 + " hour(s) - Poisson: " + SS.getTaskLookUpTable()[0][2] + " - #devices: " + j);
                        SimLogger.getInstance().simStarted(outputFolder, "SIMRESULT_" + simScenario + "_" + orchestratorPolicy + "_" + j + "DEVICES");

                        try {
                            // First step: Initialize the CloudSim package. It should be called
                            // before creating any entities.
                            int num_user = 2;   // number of grid users
                            Calendar calendar = Calendar.getInstance();
                            boolean trace_flag = false;  // mean trace events

                            // Initialize the CloudSim library
                            CloudSim.init(num_user, calendar, trace_flag, 0.01);

                            // Generate EdgeCloudsim Scenario Factory
                            ScenarioFactory sampleFactory = new SampleScenarioFactory(j, SS.getSimulationTime(), orchestratorPolicy, simScenario);

                            // Generate EdgeCloudSim Simulation Manager
                            SimManager manager = new SimManager(sampleFactory, j, simScenario, orchestratorPolicy);

                            // Start simulation
                            manager.startSimulation();
                        } catch (Exception e) {
                            SimLogger.printLine("The simulation has been terminated due to an unexpected error");
                            e.printStackTrace();
                            System.exit(0);
                        }

                        Date ScenarioEndDate = Calendar.getInstance().getTime();
                        now = df.format(ScenarioEndDate);
                        SimLogger.printLine("Scenario finished at " + now + ". It took " + SimUtils.getTimeDifference(ScenarioStartDate, ScenarioEndDate));
                        SimLogger.printLine("----------------------------------------------------------------------");
                    }//End of orchestrators loop
                }//End of scenarios loop
            }//End of mobile devices loop
        }
        Date SimulationEndDate = Calendar.getInstance().getTime();
        now = df.format(SimulationEndDate);
        SimLogger.printLine("Simulation finished at " + now + ". It took " + SimUtils.getTimeDifference(SimulationStartDate, SimulationEndDate));
    }
}
