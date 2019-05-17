package org.hvdw.jexiftoolgui.editpane;

import org.hvdw.jexiftoolgui.MyConstants;
import org.hvdw.jexiftoolgui.Utils;
import org.hvdw.jexiftoolgui.myVariables;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;


// I had specified for the array in mainScreen:
// JTextField[] geotaggingFields = {geotaggingImgFoldertextField, geotaggingGPSLogtextField, geotaggingGeosynctextField};


public class EditGeotaggingdata {

    private String ImageFolder;
    Preferences prefs = Preferences.userRoot();

    myVariables myVars = new myVariables();
    Utils myUtils = new Utils();

    public String ImgPath(JPanel myComponent) {
        String SelectedFolder;

        String startFolder = myUtils.whichFolderToOpen();
        final JFileChooser chooser = new JFileChooser(startFolder);
        chooser.setDialogTitle("Locate the image folder ...");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int status = chooser.showOpenDialog(myComponent);
        if (status == JFileChooser.APPROVE_OPTION) {
            SelectedFolder = chooser.getSelectedFile().getAbsolutePath();
            return SelectedFolder;
        } else {
            return "";
        }
    }

    public String gpsLogFile(JPanel myComponent) {

        String startFolder = myUtils.whichFolderToOpen();
        final JFileChooser chooser = new JFileChooser(startFolder);
        chooser.setMultiSelectionEnabled(false);
        String[] filexts = {"gpx", "gps", "log"};
        FileFilter filter = new FileNameExtensionFilter("(*.gpx)", filexts);
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Locate GPS log file ...");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int status = chooser.showOpenDialog(myComponent);
        if (status == JFileChooser.APPROVE_OPTION) {
            String selectedLogFile = chooser.getSelectedFile().getPath();
            return selectedLogFile;
        } else {
            return "";
        }
    }

//    public void WriteInfo(String onFolder, String gpslogfile, String geosync, boolean OverwiteOriginals, int[] selectedFilenamesIndices, File[] files) {
    public void WriteInfo(JTextField[] geotaggingFields, JCheckBox[] geotaggingBoxes, boolean OverwiteOriginals, int[] selectedFilenamesIndices, File[] files) {

        String fpath = "";
        List<String> cmdparams = new ArrayList<String>();
        JProgressBar progressBar = new JProgressBar();
        String onFolder = geotaggingFields[0].getText().trim();
        String gpslogfile = geotaggingFields[1].getText().trim();
        String geosync = geotaggingFields[2].getText().trim();

        //System.out.println("folder: " + onFolder + " gpslogfile: " + gpslogfile + " geosync: " + geosync + "OverwiteOriginals: " + OverwiteOriginals);

        // exiftool on windows or other
        String exiftool = prefs.get("exiftool", "");
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            exiftool = exiftool.replace("\\", "/");
            gpslogfile = gpslogfile.replace("\\", "/");
        }

        cmdparams.add(myUtils.platformExiftool());
        if (OverwiteOriginals) {
            cmdparams.add("-overwrite_original_in_place");
        }
        cmdparams.add("-geotag");
        cmdparams.add(gpslogfile);

        if (!"".equals(geosync)) {
            cmdparams.add("-geosync=" + geosync);
        }
        // Check if also the location is to be added
        if (geotaggingBoxes[0].isSelected()) {
            cmdparams.add("-xmp:Location=" + geotaggingFields[3].getText());
            cmdparams.add("-iptc:Sub-location=" + geotaggingFields[3].getText());
        }
        if (geotaggingBoxes[1].isSelected()) {
            cmdparams.add("-xmp:Country=" + geotaggingFields[4].getText());
            cmdparams.add("-iptc:Country-PrimaryLocationName=" + geotaggingFields[4].getText());

        }
        if (geotaggingBoxes[2].isSelected()) {
            cmdparams.add("-xmp:State=" + geotaggingFields[5].getText());
            cmdparams.add("-iptc:Province-State=" + geotaggingFields[5].getText());
        }
        if (geotaggingBoxes[3].isSelected()) {
            cmdparams.add("-xmp:City=" + geotaggingFields[6].getText());
            cmdparams.add("-iptc:City=" + geotaggingFields[6].getText());
        }
        //System.out.println("indices: " + Arrays.toString(selectedFilenamesIndices));

        if ("".equals(onFolder)) { // Empty folder string which means we use selected files
            for (int index: selectedFilenamesIndices) {
                //System.out.println("index: " + index + "  image path:" + files[index].getPath());
                if (isWindows) {
                    cmdparams.add(files[index].getPath().replace("\\", "/"));
                } else {
                    cmdparams.add(files[index].getPath());
                }
            }
        } else { // We do have a non-empty folder string
            //cmdparams.addAll( Arrays.asList(params) );
            if (isWindows) {
                cmdparams.add(onFolder.replace("\\", "/"));
            } else {
                cmdparams.add(onFolder);
            }
        }

        //System.out.println("In geotagging before runCommand");
        //System.out.println(cmdparams.toString());
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        try {
            String res = myUtils.runCommand(cmdparams);
            System.out.println(res);
            myUtils.runCommandOutput(res);
        } catch(IOException | InterruptedException ex) {
            System.out.println("Error executing command");
        }
        progressBar.setVisible(false);
    }

}