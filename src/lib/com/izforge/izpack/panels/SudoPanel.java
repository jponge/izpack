/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2003 Jan Blok (jblok@profdata.nl - PDM - www.profdata.nl)
 *
 *  File :               SudoPanel.java
 *  Description :        A panel doing a linux/unix/macosx 'sudo' for administrator (native (sub)) installs.
 *  Author's email :     jblok@profdata.nl
 *  Author's Website :   http://www.profdata.nl
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.izforge.izpack.panels;

import com.izforge.izpack.*;
import com.izforge.izpack.installer.*;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsConstraint;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;

import java.util.*;

import javax.swing.*;

/**
 *  The packs selection panel class.
 *
 * @author     Jan Blok
 * @since      November 27, 2003
 */
public class SudoPanel extends IzPanel implements ActionListener
{
	private JTextField passwordField;
	private boolean isValid = false;
	
	/**
	 *  The constructor.
	 *
	 * @param  parent  The parent window.
	 * @param  idata   The installation data.
	 */
	public SudoPanel(InstallerFrame parent, InstallData idata)
	{
		super(parent, idata);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(new JLabel(
				/*parent.langpack.getString("SudoPanel.info")*/"For installing administrator privileges are necessary",
				JLabel.TRAILING));

		add(Box.createRigidArea(new Dimension(0, 5)));
		
		add(new JLabel(
				/*parent.langpack.getString("SudoPanel.tip")*/"Please note that passwords are case-sensitive", parent.icons.getImageIcon("tip"),
				JLabel.TRAILING));

		add(Box.createRigidArea(new Dimension(0, 5)));

		JPanel spacePanel = new JPanel();
		spacePanel.setAlignmentX(LEFT_ALIGNMENT);
		spacePanel.setAlignmentY(CENTER_ALIGNMENT);
		spacePanel.setBorder(BorderFactory.createEmptyBorder(80, 30, 0, 50));
		spacePanel.setLayout(new BorderLayout(5,5));
		spacePanel.add(
			new JLabel(
				/*parent.langpack.getString("SudoPanel.specifyAdminPassword")*/"Please specify your password:"),BorderLayout.NORTH);
		passwordField = new JPasswordField();
		passwordField.addActionListener(this);
		JPanel space2Panel = new JPanel();
		space2Panel.setLayout(new BorderLayout());
		space2Panel.add(passwordField,BorderLayout.NORTH);
		space2Panel.add(Box.createRigidArea(new Dimension(0, 5)),BorderLayout.CENTER);
		spacePanel.add(space2Panel,BorderLayout.CENTER);
		add(spacePanel);
	}

	/**  Called when the panel becomes active.  */
	public void panelActivate()
	{
		passwordField.requestFocus();
	}

	/**
	 *  Actions-handling method.
	 *
	 * @param  e  The event.
	 */
	public void actionPerformed(ActionEvent e)
	{
		doSudoCmd();
	}

	//check if sudo password is correct (so sudo can be used in all other scripts, even without password, lasts for 5 minutes)
	private void doSudoCmd()
	{
		String pass = passwordField.getText();
		
		File file = null;
		try
		{
			//write file in /tmp
			file = new File("/tmp/cmd_sudo.sh");//""c:/temp/run.bat""
			FileOutputStream fos = new FileOutputStream(file);
			fos.write("echo $password | sudo -S ls\nexit $?".getBytes()); //"echo $password > pipo.txt"
			fos.close();
			
			//execute
			HashMap vars = new HashMap();
			vars.put("password", pass);

			List oses = new ArrayList();
			oses.add(new OsConstraint("unix",null,null,null));//"windows",System.getProperty("os.name"),System.getProperty("os.version"),System.getProperty("os.arch")));
			
			ArrayList plist = new ArrayList();
			ParsableFile pf = new ParsableFile(file.getAbsolutePath(),null,null,oses);
			plist.add(pf);
			ScriptParser sp = new ScriptParser(plist,new VariableSubstitutor(vars));
			sp.parseFiles();
			
			ArrayList elist = new ArrayList();
			ExecutableFile ef = new ExecutableFile(file.getAbsolutePath(),ExecutableFile.POSTINSTALL,ExecutableFile.ABORT,oses,false);
			elist.add(ef);
			FileExecutor fe = new FileExecutor(elist);
			int retval = fe.executeFiles(ExecutableFile.POSTINSTALL,this);
			if (retval == 0)
			{
				idata.setVariable("password", pass);
				isValid = true;
			}
//			else is already showing dialog
//			{
//				JOptionPane.showMessageDialog(this, "Cannot execute 'sudo' cmd, check your password", "Error", JOptionPane.ERROR_MESSAGE);
//			}
		}
		catch (Exception e)
		{
//				JOptionPane.showMessageDialog(this, "Cannot execute 'sudo' cmd, check your password", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			isValid = false;
		}
		try
		{
			if (file != null && file.exists()) file.delete();//you don't want the file with password tobe arround, in case of error
		}
		catch (Exception e)
		{
			//ignore
		}
	}

	/**
	 *  Indicates wether the panel has been validated or not.
	 *
	 * @return    Always true.
	 */
	public boolean isValidated()
	{
		if (!isValid)
		{
			doSudoCmd();
		}
		if (!isValid)
		{
			JOptionPane.showInternalMessageDialog(this, "Password", "Password is not valid", JOptionPane.ERROR_MESSAGE);
		}
		return isValid;
	}
}
