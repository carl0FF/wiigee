/*
 * wiigee - accelerometerbased gesture recognition
 * Copyright (C) 2007, 2008, 2009, 2010 Benjamin Poppinga
 *
 * Developed at University of Oldenburg
 * Contact: wiigee@benjaminpoppinga.de
 *
 * This file is part of wiigee.
 *
 * wiigee is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package wiigeegui;

import org.wiigee.control.WiimoteWiigee;
import org.wiigee.device.Wiimote;
import org.wiigee.event.AccelerationEvent;
import org.wiigee.event.ButtonPressedEvent;
import org.wiigee.event.ButtonReleasedEvent;
import org.wiigee.event.AccelerationListener;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;
import org.wiigee.event.InfraredEvent;
import org.wiigee.event.MotionStartEvent;
import org.wiigee.event.MotionStopEvent;
import org.wiigee.event.RotationEvent;
import org.wiigee.event.RotationSpeedEvent;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;
import org.wiigee.event.ButtonListener;
import org.wiigee.event.InfraredListener;
import org.wiigee.event.RotationListener;
import org.wiigee.filter.HighPassFilter;
import org.wiigee.filter.RotationResetFilter;
import org.wiigee.filter.RotationThresholdFilter;
import org.wiigee.util.Log;
import wiigeegui.Frontend;
import wiigeegui.GraphPanel;
import wiigeegui.InfraredPanel;
import wiigeegui.OrientationPanel;
import wiigeegui.WiimotePanel;

/**
 *
 * @author bepo
 */
public class Frontend extends javax.swing.JFrame implements GestureListener,
        AccelerationListener, ButtonListener, RotationListener, InfraredListener {

    private WiimoteWiigee wiigee;
    private Wiimote wiimote;
    private Vector<String> gestureMeanings;

    /** Creates new form Frontend */
    public Frontend() {
        this.wiigee = new WiimoteWiigee();
        this.gestureMeanings = new Vector<String>();
        initComponents();
    }

    public void gestureReceived(GestureEvent event) {
        if(event.isValid()) {
            this.gestureField.setBackground(Color.GREEN);
            this.gestureField.setForeground(Color.WHITE);
            this.gestureField.setText("Gesture " + this.gestureMeanings.elementAt(event.getId()) + " received.");
            this.appendToConsole("Gesture "+this.gestureMeanings.elementAt(event.getId())+" received.");
        } else {
            this.gestureField.setBackground(Color.RED);
            this.gestureField.setForeground(Color.WHITE);
            this.gestureField.setText("No Gesture recognized!");
            this.appendToConsole("No Gesture recognized!");
        }
    }

    public void accelerationReceived(AccelerationEvent event) {
        this.graphPanel1.accelerate(event);
    }

    public void buttonPressReceived(ButtonPressedEvent event) {
        this.wiimotePanel1.changeState(event);
        if (event.isRecognitionInitEvent()) {
            this.recognitionField.setBackground(Color.GREEN);
            this.gestureField.setBackground(Color.WHITE);
            this.gestureField.setForeground(Color.WHITE);
            this.gestureField.setText("");
        } else if (event.isTrainInitEvent()) {
            this.trainingField.setBackground(Color.GREEN);
            this.gestureField.setBackground(Color.WHITE);
            this.gestureField.setForeground(Color.WHITE);
            this.gestureField.setText("");
        } else if (event.isCloseGestureInitEvent()) {
            // show a dialog to enter gesture meaning
            this.getGestureMeaningDialog.setVisible(true);
        }

        // hardcoded filter reset buttons
        if(event.getButton()==Wiimote.BUTTON_MINUS) {
            Log.write("ACC FILTER RESET!");
            this.wiimote.resetAccelerationFilters();
        } else if(event.getButton()==Wiimote.BUTTON_PLUS) {
            Log.write("ROT FILTER RESET!");
            this.wiimote.resetRotationFilters();
        }
    }

    public void buttonReleaseReceived(ButtonReleasedEvent event) {
        this.wiimotePanel1.changeState(event);
        this.recognitionField.setBackground(Color.WHITE);
        this.trainingField.setBackground(Color.WHITE);
    }

    public void motionStartReceived(MotionStartEvent event) {
        this.inMotionField.setBackground(Color.GREEN);
    }

    public void motionStopReceived(MotionStopEvent event) {
        this.inMotionField.setBackground(Color.WHITE);
    }

    public void rotationSpeedReceived(RotationSpeedEvent event) {
        // nothing to do with this data
    }

    public void rotationReceived(RotationEvent event) {
        this.orientationPanel1.setRotation(event);
    }

    public void infraredReceived(InfraredEvent event) {
        this.infraredPanel1.setInfrared(event);
    }

    public void appendToConsole(String s) {
        this.consoleTextArea.append(s+"\n\r");
    }

    private void setupWiimote() {
        try {
            this.wiimote.setAccelerationEnabled(true);
            // this.wiimote.setInfraredCameraEnabled(true);
            // this.wiimote.setWiiMotionPlusEnabled(true);
            this.wiimote.addAccelerationFilter(new HighPassFilter());
            this.wiimote.addRotationFilter(new RotationThresholdFilter(0.5));
        } catch(Exception e) {
                Log.write("Error in: setupWiimote()");
                e.printStackTrace();
        }

        this.wiimote.addAccelerationListener(this);
        this.wiimote.addButtonListener(this);
        this.wiimote.addRotationListener(this);
        this.wiimote.addGestureListener(this);
        this.wiimote.addInfraredListener(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        selectWiimoteDialog = new javax.swing.JDialog();
        selectWiimotePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        wiimotesMacTextField = new javax.swing.JTextField();
        connectWiimoteButton = new javax.swing.JButton();
        scanWiimoteDialog = new javax.swing.JDialog();
        scanWiimoteStatusLabel = new javax.swing.JLabel();
        scanWiimoteApproveButton = new javax.swing.JButton();
        getGestureMeaningDialog = new javax.swing.JDialog();
        getGestureMeaningPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        gestureMeaningTextField = new javax.swing.JTextField();
        setGestureMeaningButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        gesturePanel = new javax.swing.JPanel();
        wiimotePanel1 = new wiigeegui.WiimotePanel();
        graphPanel1 = new wiigeegui.GraphPanel();
        inMotionField = new javax.swing.JTextField();
        recognitionField = new javax.swing.JTextField();
        trainingField = new javax.swing.JTextField();
        gestureField = new javax.swing.JTextField();
        consoleScrollPane = new javax.swing.JScrollPane();
        consoleTextArea = new javax.swing.JTextArea();
        accelerationLabel = new javax.swing.JLabel();
        infraredPanel = new javax.swing.JPanel();
        infraredPanel1 = new wiigeegui.InfraredPanel();
        rotationPanel = new javax.swing.JPanel();
        orientationPanel1 = new wiigeegui.OrientationPanel();
        settingsPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        infraredCheckBox = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        wiiMotionPlusCheckBox = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jCheckBox4 = new javax.swing.JCheckBox();
        accelerationCheckBox = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        robotMouseCheckBox = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        wiimoteMenu = new javax.swing.JMenu();
        connectWiimoteItem = new javax.swing.JMenuItem();
        autoconnectWiimoteItem = new javax.swing.JMenuItem();
        disconnectWiimoteItem = new javax.swing.JMenuItem();
        fileMenu = new javax.swing.JMenu();
        loadGesturesItem = new javax.swing.JMenuItem();
        saveGesturesItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutItem = new javax.swing.JMenuItem();

        jFileChooser1.setAcceptAllFileFilterUsed(false);
        jFileChooser1.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".wgs") || f.isDirectory();
            }

            public String getDescription() {
                return "wiigee Gestureset (*.wgs)";
            }
        });

        selectWiimoteDialog.setTitle("Connect");
        selectWiimoteDialog.setModal(true);
        selectWiimoteDialog.setName("selectWiimoteDialog"); // NOI18N

        selectWiimotePanel.setMinimumSize(new java.awt.Dimension(315, 125));

        jLabel1.setText("Please enter the Wiimote's Bluetooth MAC:");

        wiimotesMacTextField.setText("00:1E:35:0F:40:BA");

        connectWiimoteButton.setText("Connect");
        connectWiimoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectWiimoteButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout selectWiimotePanelLayout = new org.jdesktop.layout.GroupLayout(selectWiimotePanel);
        selectWiimotePanel.setLayout(selectWiimotePanelLayout);
        selectWiimotePanelLayout.setHorizontalGroup(
            selectWiimotePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, selectWiimotePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(selectWiimotePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(connectWiimoteButton)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, wiimotesMacTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
                .addContainerGap())
        );
        selectWiimotePanelLayout.setVerticalGroup(
            selectWiimotePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectWiimotePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(wiimotesMacTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connectWiimoteButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout selectWiimoteDialogLayout = new org.jdesktop.layout.GroupLayout(selectWiimoteDialog.getContentPane());
        selectWiimoteDialog.getContentPane().setLayout(selectWiimoteDialogLayout);
        selectWiimoteDialogLayout.setHorizontalGroup(
            selectWiimoteDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectWiimotePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        selectWiimoteDialogLayout.setVerticalGroup(
            selectWiimoteDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectWiimotePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        selectWiimoteDialog.getAccessibleContext().setAccessibleParent(this);

        scanWiimoteDialog.setTitle("Scanning...");
        scanWiimoteDialog.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        scanWiimoteDialog.setEnabled(false);
        scanWiimoteDialog.setModal(true);
        scanWiimoteDialog.setName("scanWiimote"); // NOI18N
        scanWiimoteDialog.setUndecorated(true);

        scanWiimoteStatusLabel.setText("Please press Button A and B on the Wiimote...");

        scanWiimoteApproveButton.setText("OK");
        scanWiimoteApproveButton.setEnabled(false);
        scanWiimoteApproveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanWiimoteApproveButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout scanWiimoteDialogLayout = new org.jdesktop.layout.GroupLayout(scanWiimoteDialog.getContentPane());
        scanWiimoteDialog.getContentPane().setLayout(scanWiimoteDialogLayout);
        scanWiimoteDialogLayout.setHorizontalGroup(
            scanWiimoteDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scanWiimoteDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(scanWiimoteStatusLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, scanWiimoteDialogLayout.createSequentialGroup()
                .addContainerGap(232, Short.MAX_VALUE)
                .add(scanWiimoteApproveButton)
                .addContainerGap())
        );
        scanWiimoteDialogLayout.setVerticalGroup(
            scanWiimoteDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scanWiimoteDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(scanWiimoteStatusLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scanWiimoteApproveButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getGestureMeaningDialog.setModal(true);

        jLabel2.setText("Please enter the meaning of the trained gesture:");

        gestureMeaningTextField.setText("e.g. Circle");

        setGestureMeaningButton.setText("OK");
        setGestureMeaningButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setGestureMeaningButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout getGestureMeaningPanelLayout = new org.jdesktop.layout.GroupLayout(getGestureMeaningPanel);
        getGestureMeaningPanel.setLayout(getGestureMeaningPanelLayout);
        getGestureMeaningPanelLayout.setHorizontalGroup(
            getGestureMeaningPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, getGestureMeaningPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(getGestureMeaningPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(setGestureMeaningButton)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, gestureMeaningTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        getGestureMeaningPanelLayout.setVerticalGroup(
            getGestureMeaningPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(getGestureMeaningPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gestureMeaningTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(setGestureMeaningButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout getGestureMeaningDialogLayout = new org.jdesktop.layout.GroupLayout(getGestureMeaningDialog.getContentPane());
        getGestureMeaningDialog.getContentPane().setLayout(getGestureMeaningDialogLayout);
        getGestureMeaningDialogLayout.setHorizontalGroup(
            getGestureMeaningDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(getGestureMeaningPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        getGestureMeaningDialogLayout.setVerticalGroup(
            getGestureMeaningDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(getGestureMeaningPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("wiigee Demo-GUI");
        setLocationByPlatform(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        wiimotePanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.jdesktop.layout.GroupLayout wiimotePanel1Layout = new org.jdesktop.layout.GroupLayout(wiimotePanel1);
        wiimotePanel1.setLayout(wiimotePanel1Layout);
        wiimotePanel1Layout.setHorizontalGroup(
            wiimotePanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 158, Short.MAX_VALUE)
        );
        wiimotePanel1Layout.setVerticalGroup(
            wiimotePanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 712, Short.MAX_VALUE)
        );

        graphPanel1.setBackground(new java.awt.Color(255, 255, 255));
        graphPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.jdesktop.layout.GroupLayout graphPanel1Layout = new org.jdesktop.layout.GroupLayout(graphPanel1);
        graphPanel1.setLayout(graphPanel1Layout);
        graphPanel1Layout.setHorizontalGroup(
            graphPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 437, Short.MAX_VALUE)
        );
        graphPanel1Layout.setVerticalGroup(
            graphPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 241, Short.MAX_VALUE)
        );

        inMotionField.setEditable(false);
        inMotionField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        inMotionField.setText("In Motion");

        recognitionField.setEditable(false);
        recognitionField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        recognitionField.setText("Recognition");

        trainingField.setEditable(false);
        trainingField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        trainingField.setText("Training");

        gestureField.setEditable(false);
        gestureField.setFont(new java.awt.Font("Lucida Grande", 1, 15));
        gestureField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        consoleScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        consoleTextArea.setColumns(20);
        consoleTextArea.setLineWrap(true);
        consoleTextArea.setRows(5);
        consoleScrollPane.setViewportView(consoleTextArea);

        accelerationLabel.setText("Acceleration:");

        org.jdesktop.layout.GroupLayout gesturePanelLayout = new org.jdesktop.layout.GroupLayout(gesturePanel);
        gesturePanel.setLayout(gesturePanelLayout);
        gesturePanelLayout.setHorizontalGroup(
            gesturePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gesturePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(wiimotePanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gesturePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(graphPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, consoleScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .add(gestureField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, gesturePanelLayout.createSequentialGroup()
                        .add(inMotionField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(recognitionField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(trainingField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, accelerationLabel))
                .addContainerGap())
        );
        gesturePanelLayout.setVerticalGroup(
            gesturePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gesturePanelLayout.createSequentialGroup()
                .add(gesturePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, gesturePanelLayout.createSequentialGroup()
                        .add(accelerationLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(graphPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gesturePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE, false)
                            .add(inMotionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(recognitionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(trainingField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gestureField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(consoleScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 355, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(wiimotePanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Gestures", gesturePanel);

        org.jdesktop.layout.GroupLayout infraredPanel1Layout = new org.jdesktop.layout.GroupLayout(infraredPanel1);
        infraredPanel1.setLayout(infraredPanel1Layout);
        infraredPanel1Layout.setHorizontalGroup(
            infraredPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 649, Short.MAX_VALUE)
        );
        infraredPanel1Layout.setVerticalGroup(
            infraredPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 734, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout infraredPanelLayout = new org.jdesktop.layout.GroupLayout(infraredPanel);
        infraredPanel.setLayout(infraredPanelLayout);
        infraredPanelLayout.setHorizontalGroup(
            infraredPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(infraredPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        infraredPanelLayout.setVerticalGroup(
            infraredPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(infraredPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Infrared", infraredPanel);

        org.jdesktop.layout.GroupLayout rotationPanelLayout = new org.jdesktop.layout.GroupLayout(rotationPanel);
        rotationPanel.setLayout(rotationPanelLayout);
        rotationPanelLayout.setHorizontalGroup(
            rotationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(orientationPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 649, Short.MAX_VALUE)
        );
        rotationPanelLayout.setVerticalGroup(
            rotationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(orientationPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 734, Short.MAX_VALUE)
        );

        orientationPanel1.getAccessibleContext().setAccessibleName("orientationPanel1");

        jTabbedPane1.addTab("Rotation", rotationPanel);

        jLabel3.setText("Infrared:");

        jLabel4.setText("Vibration:");

        jCheckBox1.setText("On");

        infraredCheckBox.setText("On");
        infraredCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infraredCheckBoxActionPerformed(evt);
            }
        });

        jLabel5.setText("Wii MotionPlus:");

        wiiMotionPlusCheckBox.setText("On");
        wiiMotionPlusCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wiiMotionPlusCheckBoxActionPerformed(evt);
            }
        });

        jLabel6.setText("Acceleration:");

        jLabel7.setText("Buttons:");

        jCheckBox4.setSelected(true);
        jCheckBox4.setText("On");
        jCheckBox4.setEnabled(false);

        accelerationCheckBox.setSelected(true);
        accelerationCheckBox.setText("On");
        accelerationCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accelerationCheckBoxActionPerformed(evt);
            }
        });

        jLabel8.setText("IR Mouse:");

        robotMouseCheckBox.setText("On");
        robotMouseCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                robotMouseCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout settingsPanelLayout = new org.jdesktop.layout.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(settingsPanelLayout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(wiiMotionPlusCheckBox))
                    .add(settingsPanelLayout.createSequentialGroup()
                        .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel7)
                            .add(jLabel6)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(accelerationCheckBox)
                            .add(jCheckBox4)
                            .add(infraredCheckBox))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 333, Short.MAX_VALUE)
                .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel4)
                    .add(jLabel8))
                .add(18, 18, 18)
                .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBox1)
                    .add(robotMouseCheckBox))
                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(settingsPanelLayout.createSequentialGroup()
                        .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jCheckBox1)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel8)
                            .add(robotMouseCheckBox)))
                    .add(settingsPanelLayout.createSequentialGroup()
                        .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jCheckBox4)
                            .add(jLabel7))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel6)
                            .add(accelerationCheckBox))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(infraredCheckBox)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(wiiMotionPlusCheckBox)
                            .add(jLabel5))))
                .addContainerGap(619, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Settings", settingsPanel);

        wiimoteMenu.setText("Wiimote");

        connectWiimoteItem.setText("Connect...");
        connectWiimoteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectWiimoteItemActionPerformed(evt);
            }
        });
        wiimoteMenu.add(connectWiimoteItem);

        autoconnectWiimoteItem.setText("Autoconnect");
        autoconnectWiimoteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoconnectWiimoteItemActionPerformed(evt);
            }
        });
        wiimoteMenu.add(autoconnectWiimoteItem);

        disconnectWiimoteItem.setText("Disconnect");
        disconnectWiimoteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectWiimoteItemActionPerformed(evt);
            }
        });
        wiimoteMenu.add(disconnectWiimoteItem);

        jMenuBar1.add(wiimoteMenu);

        fileMenu.setText("File");

        loadGesturesItem.setText("Load Gestureset");
        loadGesturesItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadGesturesItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadGesturesItem);

        saveGesturesItem.setText("Save Gestureset");
        saveGesturesItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveGesturesItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveGesturesItem);

        jMenuBar1.add(fileMenu);

        helpMenu.setText("Help");

        aboutItem.setText("About");
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 670, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveGesturesItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveGesturesItemActionPerformed
        BufferedWriter out = null;
        try {
            int retVal = this.jFileChooser1.showSaveDialog(fileMenu);

            if (retVal == jFileChooser1.APPROVE_OPTION) {
                // save the gesture set file
                File f = jFileChooser1.getSelectedFile();
                out = new BufferedWriter(new FileWriter(f));

                // save the specified gestures and write text
                for (int i = 0; i < this.gestureMeanings.size(); i++) {
                    this.wiimote.saveGesture(i, f.getParent()+"/"+this.gestureMeanings.elementAt(i));
                    out.write(this.gestureMeanings.elementAt(i));
                    out.newLine();
                }
                
                out.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(Frontend.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Frontend.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_saveGesturesItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (wiimote != null) {
            wiimote.disconnect();
        }
    }//GEN-LAST:event_formWindowClosing

    private void autoconnectWiimoteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoconnectWiimoteItemActionPerformed
        //this.scanWiimoteDialog.setVisible(true);
        try {
            Wiimote wm = this.wiigee.getDevice();
            if (wm != null) {
                this.scanWiimoteStatusLabel.setText("Found a Wiimote!");
                this.scanWiimoteApproveButton.setEnabled(true);
                this.wiimote = wm;
                this.setupWiimote();
            }
        } catch (IOException ex) {
            this.scanWiimoteStatusLabel.setText("No Wiimote found!");
            this.scanWiimoteApproveButton.setEnabled(true);
            Logger.getLogger(Frontend.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_autoconnectWiimoteItemActionPerformed

    private void connectWiimoteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectWiimoteItemActionPerformed
        this.selectWiimoteDialog.setVisible(true);
    }//GEN-LAST:event_connectWiimoteItemActionPerformed

    private void disconnectWiimoteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectWiimoteItemActionPerformed
        if (this.wiimote != null) {
            this.wiimote.disconnect();
        }
    }//GEN-LAST:event_disconnectWiimoteItemActionPerformed

    private void scanWiimoteApproveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanWiimoteApproveButtonActionPerformed
        this.scanWiimoteDialog.setVisible(false);
    }//GEN-LAST:event_scanWiimoteApproveButtonActionPerformed

    private void connectWiimoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectWiimoteButtonActionPerformed
        try { // manual connect: 001E350F40BA
            this.selectWiimoteDialog.setVisible(false);
            this.wiimote = new Wiimote(this.wiimotesMacTextField.getText(), true, true);
            this.setupWiimote();
        } catch (IOException ex) {
            Logger.getLogger(Frontend.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_connectWiimoteButtonActionPerformed

    private void setGestureMeaningButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setGestureMeaningButtonActionPerformed
        this.gestureMeanings.add(this.gestureMeaningTextField.getText());
        this.getGestureMeaningDialog.setVisible(false);
    }//GEN-LAST:event_setGestureMeaningButtonActionPerformed

    private void loadGesturesItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadGesturesItemActionPerformed
        BufferedReader in = null;
        try {
            int retVal = this.jFileChooser1.showOpenDialog(fileMenu);

            if (retVal == jFileChooser1.APPROVE_OPTION) {
                // open the gesture set file
                File f = jFileChooser1.getSelectedFile();
                in = new BufferedReader(new FileReader(f));

                // load the single gestures
                String line;
                while(in.ready()) {
                    line = in.readLine();
                    this.wiimote.loadGesture(f.getParent()+"/"+line);
                    this.gestureMeanings.add(line);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Frontend.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Frontend.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_loadGesturesItemActionPerformed

    private void infraredCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infraredCheckBoxActionPerformed
        try {
            this.wiimote.setInfraredCameraEnabled(infraredCheckBox.isSelected());
        } catch(Exception e) {
            Log.write("Error while activating Infrared Camera:");
            e.printStackTrace();
        }
    }//GEN-LAST:event_infraredCheckBoxActionPerformed

    private void wiiMotionPlusCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wiiMotionPlusCheckBoxActionPerformed
        try {
            this.wiimote.setWiiMotionPlusEnabled(wiiMotionPlusCheckBox.isSelected());
        } catch(Exception e) {
            Log.write("Error while activating Wii MotionPlus:");
            e.printStackTrace();
        }
    }//GEN-LAST:event_wiiMotionPlusCheckBoxActionPerformed

    private void accelerationCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accelerationCheckBoxActionPerformed
        try {
            this.wiimote.setAccelerationEnabled(accelerationCheckBox.isSelected());
        } catch(Exception e) {
            Log.write("Error while activating Acceleration:");
            e.printStackTrace();
        }
    }//GEN-LAST:event_accelerationCheckBoxActionPerformed

    private void robotMouseCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_robotMouseCheckBoxActionPerformed
        this.infraredPanel1.setRobotMouseEnabled(robotMouseCheckBox.isSelected());
    }//GEN-LAST:event_robotMouseCheckBoxActionPerformed

    private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutItemActionPerformed
        
    }//GEN-LAST:event_aboutItemActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutItem;
    private javax.swing.JCheckBox accelerationCheckBox;
    private javax.swing.JLabel accelerationLabel;
    private javax.swing.JMenuItem autoconnectWiimoteItem;
    private javax.swing.JButton connectWiimoteButton;
    private javax.swing.JMenuItem connectWiimoteItem;
    private javax.swing.JScrollPane consoleScrollPane;
    private javax.swing.JTextArea consoleTextArea;
    private javax.swing.JMenuItem disconnectWiimoteItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JTextField gestureField;
    private javax.swing.JTextField gestureMeaningTextField;
    private javax.swing.JPanel gesturePanel;
    private javax.swing.JDialog getGestureMeaningDialog;
    private javax.swing.JPanel getGestureMeaningPanel;
    private wiigeegui.GraphPanel graphPanel1;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JTextField inMotionField;
    private javax.swing.JCheckBox infraredCheckBox;
    private javax.swing.JPanel infraredPanel;
    private wiigeegui.InfraredPanel infraredPanel1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JMenuItem loadGesturesItem;
    private wiigeegui.OrientationPanel orientationPanel1;
    private javax.swing.JTextField recognitionField;
    private javax.swing.JCheckBox robotMouseCheckBox;
    private javax.swing.JPanel rotationPanel;
    private javax.swing.JMenuItem saveGesturesItem;
    private javax.swing.JButton scanWiimoteApproveButton;
    private javax.swing.JDialog scanWiimoteDialog;
    private javax.swing.JLabel scanWiimoteStatusLabel;
    private javax.swing.JDialog selectWiimoteDialog;
    private javax.swing.JPanel selectWiimotePanel;
    private javax.swing.JButton setGestureMeaningButton;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JTextField trainingField;
    private javax.swing.JCheckBox wiiMotionPlusCheckBox;
    private javax.swing.JMenu wiimoteMenu;
    private wiigeegui.WiimotePanel wiimotePanel1;
    private javax.swing.JTextField wiimotesMacTextField;
    // End of variables declaration//GEN-END:variables


}
