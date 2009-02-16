package com.izforge.izpack.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.util.Debug;


public class FileInputField extends JPanel implements ActionListener
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
        this.initialize();
    }
    
    public void initialize(){
        filetxt = new JTextField(set, size);
        filetxt.setCaretPosition(0);        
        
        // TODO: use separate key for button text
        browseBtn = ButtonFactory.createButton(data.langpack.getString("UserInputPanel.search.browse"), data.buttonsHColor);
        browseBtn.addActionListener(this);        
        this.add(filetxt);
        this.add(browseBtn);           
    }
    
//    final UserInputFileFilter uiff = new UserInputFileFilter();
//    uiff.setFileExt(filter);
//    uiff.setFileExtDesc(filterdesc);
//
//    // TODO: use separate key for button text
//    JButton button = ButtonFactory.createButton(idata.langpack.getString("UserInputPanel.search.browse"), idata.buttonsHColor);
//    button.addActionListener(new ActionListener() {
//        public void actionPerformed(ActionEvent e) {
//            System.out.println("Show filechooser");
//            JFileChooser filechooser = new JFileChooser(currentDirectoryPath);
//            filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//            filechooser.setFileFilter(uiff);
//
//            if (filechooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
//                filetxt.setText(filechooser.getSelectedFile().getAbsolutePath());
//                currentDirectoryPath = filechooser.getSelectedFile().getParent();
//                Debug.trace("Setting current file chooser directory to: " + currentDirectoryPath);
//            }
//        }
//    });
//    JPanel panel = new JPanel();
//    panel.add(filetxt);
//    panel.add(button);


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
}
