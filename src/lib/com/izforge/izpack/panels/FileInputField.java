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
import java.io.*;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsVersion;

public class FileInputField extends JPanel implements ActionListener, FocusListener
{

    private static final long serialVersionUID = 4673684743657328492L;

    boolean isDirectory;

    InstallerFrame parentFrame;

    IzPanel parent;

    List<ValidatorContainer> validators;

    JTextField filetxt;

    JButton browseBtn;

    String set;

    int size;

    InstallData data;

    String fileExtension;

    String fileExtensionDescription;

    boolean allowEmpty;

    boolean mustExist;

    public FileInputField(IzPanel parent, InstallData data, boolean directory, boolean mustExist,
            String set, int size, List<ValidatorContainer> validatorConfig)
    {
        this(parent, data, directory, mustExist, set, size, validatorConfig, null, null);
    }

    public FileInputField(IzPanel parent, InstallData data, boolean directory, boolean mustExist,
            String set, int size, List<ValidatorContainer> validatorConfig, String fileExt,
            String fileExtDesc)
    {
        this.parent = parent;
        this.parentFrame = parent.getInstallerFrame();
        this.data = data;
        this.validators = validatorConfig;
        this.set = set;
        this.size = size;
        this.fileExtension = fileExt;
        this.fileExtensionDescription = fileExtDesc;
        this.isDirectory = directory;
        this.mustExist = mustExist;
        this.initialize();
    }

    public void initialize()
    {
        filetxt = new JTextField(set, size);
        filetxt.setCaretPosition(0);
        filetxt.addFocusListener(this);

        // TODO: use separate key for button text
        browseBtn = ButtonFactory.createButton(data.langpack
                .getString("UserInputPanel.search.browse"), data.buttonsHColor);
        browseBtn.addActionListener(this);
        this.add(filetxt);
        this.add(browseBtn);
    }

    public void setFile(String filename)
    {
        filetxt.setText(filename);
    }

    public void actionPerformed(ActionEvent arg0)
    {
        if (arg0.getSource() == browseBtn)
        {
            Debug.trace("Show dirchooser");
            String initialPath = ".";
            if (filetxt.getText() != null)
            {
                initialPath = filetxt.getText();
            }
            JFileChooser filechooser = new JFileChooser(initialPath);
            if (isDirectory)
            {
                filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            else
            {
                filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if ((fileExtension != null) && (fileExtensionDescription != null))
                {
                    UserInputFileFilter fileFilter = new UserInputFileFilter();
                    fileFilter.setFileExt(fileExtension);
                    fileFilter.setFileExtDesc(fileExtensionDescription);
                    filechooser.setFileFilter(fileFilter);
                }
            }

            if (filechooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION)
            {
                String selectedFile = filechooser.getSelectedFile().getAbsolutePath();
                filetxt.setText(selectedFile);
                Debug.trace("Setting current file chooser directory to: " + selectedFile);
            }
        }
    }

    public File getSelectedFile()
    {
        File result = null;
        if (filetxt.getText() != null)
        {
            result = new File(filetxt.getText());
        }
        return result;
    }

    private void showMessage(String messageType)
    {
        JOptionPane.showMessageDialog(parentFrame, parentFrame.langpack.getString("UserInputPanel."
                + messageType + ".message"), parentFrame.langpack.getString("UserInputPanel."
                + messageType + ".caption"), JOptionPane.WARNING_MESSAGE);
    }

    public boolean validateField()
    {
        boolean result = false;
        String input = filetxt.getText();
        if (allowEmpty && ((input == null) || (input.length() == 0)))
        {
            result = true;
        }
        else if (input != null)
        {
            File file = new File(input);

            if (isDirectory && !file.isDirectory())
            {
                if (mustExist)
                {
                    result = false;
                    showMessage("dir.notdirectory");
                }
                else
                {
                    result = verifyCreateOK();
                }
            }
            else if (!isDirectory && !file.isFile())
            {
                if (mustExist)
                {
                    result = false;
                    showMessage("file.notfile");
                }
                else
                {
                    result = verifyCreateOK();
                }
            }
            else
            {
                StringInputProcessingClient processingClient = new StringInputProcessingClient(
                        input, validators);
                boolean success = processingClient.validate();
                if (!success)
                {
                    JOptionPane
                            .showMessageDialog(parentFrame,
                                    processingClient.getValidationMessage(), parentFrame.langpack
                                            .getString("UserInputPanel.error.caption"),
                                    JOptionPane.WARNING_MESSAGE);
                }
                result = success;
            }
        }
        else
        {
            if (isDirectory)
            {
                showMessage("dir.nodirectory");
            }
            else
            {
                showMessage("file.nofile");
            }
        }
        return result;
    }

    public boolean verifyCreateOK()
    {
        String chosenPath = filetxt.getText();

        // Expand unix home reference
        if (chosenPath.startsWith("~"))
        {
            String home = System.getProperty("user.home");
            chosenPath = home + chosenPath.substring(1);
        }

        // Normalize the path
        File path = new File(chosenPath).getAbsoluteFile();
        chosenPath = path.toString();

        filetxt.setText(chosenPath);

        if (!path.exists())
        {
            if (!parent.emitNotificationFeedback(parent.getI18nStringForClass("createdir",
                    "TargetPanel")
                    + "\n" + chosenPath)) return false;
        }

        // We assume, that we would install something into this dir
        if (!isWriteable())
        {
            parent.emitError(parentFrame.langpack.getString("installer.error"), parent
                    .getI18nStringForClass("notwritable", "TargetPanel"));
            return false;
        }
        return true;
    }

    /**
     * This method determines whether the chosen dir is writeable or not.
     * 
     * @return whether the chosen dir is writeable or not
     */
    public boolean isWriteable()
    {
        File existParent = IoHelper.existingParent(new File(filetxt.getText()));
        if (existParent == null) { return false; }

        // On windows we cannot use canWrite because
        // it looks to the dos flags which are not valid
        // on NT or 2k XP or ...
        if (OsVersion.IS_WINDOWS)
        {
            File tmpFile;
            try
            {
                tmpFile = File.createTempFile("izWrTe", ".tmp", existParent);
                tmpFile.deleteOnExit();
            }
            catch (IOException e)
            {
                Debug.trace(e.toString());
                return false;
            }
            return true;
        }
        return existParent.canWrite();
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
        if (e.getSource() == this.filetxt)
        {

        }
    }
}
