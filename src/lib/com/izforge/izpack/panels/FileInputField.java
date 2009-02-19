/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 * 
 * Copyright 2009 Dennis Reil
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.util.Debug;


public class FileInputField extends JPanel implements ActionListener, FocusListener
{
    private static final long serialVersionUID = 4673684743657328492L;
    
    boolean isDirectory;
    InstallerFrame parentFrame;
    List<ValidatorContainer> validators;
    
    JTextField filetxt;
    JButton browseBtn;
    
    String set;
    int size;
    InstallData data;
    String fileExtension;
    String fileExtensionDescription;
    
    boolean allowEmpty;        
    
    public FileInputField(InstallerFrame parent, InstallData data, boolean directory, String set, int size,List<ValidatorContainer> validatorConfig){
        this(parent,data,directory,set,size,validatorConfig,null,null);
    }
    
    public FileInputField(InstallerFrame parent, InstallData data, boolean directory, String set, int size,List<ValidatorContainer> validatorConfig,String fileExt, String fileExtDesc){
        this.parentFrame = parent;
        this.data = data;
        this.validators = validatorConfig;
        this.set = set;
        this.size = size;
        this.fileExtension = fileExt;
        this.fileExtensionDescription = fileExtDesc;
        this.isDirectory = directory;
        this.initialize();
    }
    
    public void initialize(){
        filetxt = new JTextField(set, size);
        filetxt.setCaretPosition(0);        
        filetxt.addFocusListener(this);
        
        // TODO: use separate key for button text
        browseBtn = ButtonFactory.createButton(data.langpack.getString("UserInputPanel.search.browse"), data.buttonsHColor);
        browseBtn.addActionListener(this);        
        this.add(filetxt);
        this.add(browseBtn);           
    }
    
    public void setFile(String filename){
        filetxt.setText(filename);
    }

    public void actionPerformed(ActionEvent arg0)
    {
        if (arg0.getSource() == browseBtn){
            Debug.trace("Show dirchooser");
            String initialPath = ".";
            if (filetxt.getText() != null){
                initialPath = filetxt.getText();
            }
            JFileChooser filechooser = new JFileChooser(initialPath);
            if (isDirectory){
                filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);    
            }
            else {
                filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if ((fileExtension != null) && (fileExtensionDescription != null)){
                    UserInputFileFilter fileFilter = new UserInputFileFilter();
                    fileFilter.setFileExt(fileExtension);
                    fileFilter.setFileExtDesc(fileExtensionDescription);
                    filechooser.setFileFilter(fileFilter);    
                }                
            }            

            if (filechooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
                String selectedFile = filechooser.getSelectedFile().getAbsolutePath();
                filetxt.setText(selectedFile);
                Debug.trace("Setting current file chooser directory to: " + selectedFile);
            }    
        }                
    }    
    
    public File getSelectedFile(){
        File result = null;
        if (filetxt.getText() != null){
            result = new File(filetxt.getText());
        }
        return result;            
    }
    
    private void showMessage(String messageType) {
        JOptionPane.showMessageDialog(parentFrame, parentFrame.langpack.getString("UserInputPanel." + messageType + ".message"),
                parentFrame.langpack.getString("UserInputPanel." + messageType + ".caption"),
                JOptionPane.WARNING_MESSAGE);
    }
    
    public boolean validateField(){        
        boolean result = false;
        String input = filetxt.getText();
        if (allowEmpty && ((input == null) || (input.length() == 0))){
            result = true;
        }
        else if (input != null){
            File file = new File(input);
            
            if (isDirectory && !file.isDirectory()){
                result = false;
                showMessage("dir.notdirectory");
            }
            else if (!isDirectory && !file.isFile()){
                result = false;
                showMessage("file.notfile");
            }
            else {
                StringInputProcessingClient processingClient = new StringInputProcessingClient(input,validators);
                boolean success = processingClient.validate();
                if (!success){
                    JOptionPane.showMessageDialog(parentFrame, processingClient.getValidationMessage(),
                            parentFrame.langpack.getString("UserInputPanel.error.caption"),
                            JOptionPane.WARNING_MESSAGE);    
                }
                result = success;
            }
        }
        else {
            if (isDirectory){
                showMessage("dir.nodirectory");    
            }
            else {
                showMessage("file.nofile");
            }            
        }
        return result;
        
    }

    
    public boolean isAllowEmptyInput()
    {
        return allowEmpty;
    }

    
    public void setAllowEmptyInput(boolean allowEmpty)
    {
        this.allowEmpty = allowEmpty;
    }

    public void focusGained(FocusEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    public void focusLost(FocusEvent e)
    {
        if (e.getSource() == this.filetxt){
            
        }        
    }
}
