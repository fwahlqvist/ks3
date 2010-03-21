 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 
/*
 * Created on 19-jun-2003
 *
 */

package pdi.jobentry.SendToS3;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import org.jets3t.service.Constants;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.EmailAddressGrantee;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.multithread.DownloadPackage;
import org.jets3t.service.multithread.S3ServiceSimpleMulti;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.AWSDevPayCredentials;
import org.jets3t.service.utils.ServiceUtils;


/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql script to be executed)
 *  
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntrySendToS3Dialog extends JobEntryDialog implements JobEntryDialogInterface
{
  
    private Label        wlName;
    private Text         wName;
    private FormData     fdlName, fdName;
  
    private Label        wlAccessKey;
    private TextVar      wAccessKey;
    private FormData     fdlAccessKey, fdAccessKey;
    
	private Label        wlPrivateKey;
	private TextVar      wPrivateKey;
	private FormData     fdlPrivateKey, fdPrivateKey;
	
	private Label        wlS3Bucket;
	private TextVar      wS3Bucket;
	private FormData     fdlS3Bucket, fdS3Bucket;
	
	private Label        wlFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdFilename;
	
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntrySendToS3     jobEntry;
	private Shell       	shell;
	private PropsUI       	props;

	private SelectionAdapter lsDef;

	private boolean changed;
	
	public JobEntrySendToS3Dialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
	{
			super(parent, jobEntryInt, rep, jobMeta);
			props=PropsUI.getInstance();
			this.jobEntry=(JobEntrySendToS3) jobEntryInt;
	
			if (this.jobEntry.getName() == null) this.jobEntry.setName(jobEntryInt.getName());
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				jobEntry.setChanged();
			}
		};
		changed = jobEntry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Send Files to S3");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
        
        // Jobname line
        wlName=new Label(shell, SWT.RIGHT);
        wlName.setText("Job entry name ");
        props.setLook(wlName);
        fdlName=new FormData();
        fdlName.left = new FormAttachment(0, 0);
        fdlName.right= new FormAttachment(middle, -margin);
        fdlName.top  = new FormAttachment(0, margin);
        wlName.setLayoutData(fdlName);
        wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        fdName=new FormData();
        fdName.left = new FormAttachment(middle, 0);
        fdName.top  = new FormAttachment(0, margin);
        fdName.right= new FormAttachment(100, 0);
        wName.setLayoutData(fdName);
        
        // AccessKey line
        wlAccessKey=new Label(shell, SWT.RIGHT);
        wlAccessKey.setText("Access Key");
        props.setLook(wlAccessKey);
        fdlAccessKey=new FormData();
        fdlAccessKey.left = new FormAttachment(0, 0);
        fdlAccessKey.top  = new FormAttachment(wName, margin);
        fdlAccessKey.right= new FormAttachment(middle, -margin);
        wlAccessKey.setLayoutData(fdlAccessKey);
        wAccessKey=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wAccessKey);
        wAccessKey.addModifyListener(lsMod);
        fdAccessKey=new FormData();
        fdAccessKey.left = new FormAttachment(middle, 0);
        fdAccessKey.top  = new FormAttachment(wName, margin);
        fdAccessKey.right= new FormAttachment(100, 0);
        wAccessKey.setLayoutData(fdAccessKey);        

		// PrivateKey line
		wlPrivateKey=new Label(shell, SWT.RIGHT);
		wlPrivateKey.setText("Private Key");
 		props.setLook(wlPrivateKey);
		fdlPrivateKey=new FormData();
		fdlPrivateKey.left = new FormAttachment(0, 0);
		fdlPrivateKey.top  = new FormAttachment(wAccessKey, margin);
		fdlPrivateKey.right= new FormAttachment(middle, -margin);
		wlPrivateKey.setLayoutData(fdlPrivateKey);
		wPrivateKey=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPrivateKey);
		wPrivateKey.addModifyListener(lsMod);
		fdPrivateKey=new FormData();
		fdPrivateKey.left = new FormAttachment(middle, 0);
		fdPrivateKey.top  = new FormAttachment(wAccessKey, margin);
		fdPrivateKey.right= new FormAttachment(100, 0);
		wPrivateKey.setLayoutData(fdPrivateKey);

		// S3Bucket line
		wlS3Bucket=new Label(shell, SWT.RIGHT);
		wlS3Bucket.setText("S3Bucket");
 		props.setLook(wlS3Bucket);
		fdlS3Bucket=new FormData();
		fdlS3Bucket.left = new FormAttachment(0, 0);
		fdlS3Bucket.top  = new FormAttachment(wPrivateKey, margin);
		fdlS3Bucket.right= new FormAttachment(middle, -margin);
		wlS3Bucket.setLayoutData(fdlS3Bucket);
		wS3Bucket=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wS3Bucket);
		wS3Bucket.addModifyListener(lsMod);
		fdS3Bucket=new FormData();
		fdS3Bucket.left = new FormAttachment(middle, 0);
		fdS3Bucket.top  = new FormAttachment(wPrivateKey, margin);
		fdS3Bucket.right= new FormAttachment(100, 0);
		wS3Bucket.setLayoutData(fdS3Bucket);

		// Filename line
			wlFilename=new Label(shell, SWT.RIGHT);
		wlFilename.setText("Filename");
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wS3Bucket, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);
		wFilename=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top  = new FormAttachment(wS3Bucket, margin);
		fdFilename.right= new FormAttachment(100, 0);
		wFilename.setLayoutData(fdFilename);
		
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFilename);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
	
        wAccessKey.addSelectionListener( lsDef );
        wPrivateKey.addSelectionListener( lsDef );
        wS3Bucket.addSelectionListener( lsDef );
        wFilename.addSelectionListener(lsDef);
        			
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
				
		getData();
		
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
		    if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();

        wAccessKey.setText(Const.NVL(jobEntry.getAccessKey(), ""));
		wPrivateKey.setText(Const.NVL(jobEntry.getPrivateKey(), ""));
		wS3Bucket.setText(Const.NVL(jobEntry.getS3Bucket(), ""));
		wFilename.setText(Const.NVL(jobEntry.GetFilenameToSend(), ""));
	}
	
	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}
	
	private void ok()
	{
		jobEntry.setName(wName.getText());
        jobEntry.setAccessKey(wAccessKey.getText());
		jobEntry.setPrivateKey(wPrivateKey.getText());
		jobEntry.SetFilenameToSend(wFilename.getText());
		jobEntry.setS3Bucket(wS3Bucket.getText());

		dispose();
	}

	public String toString()
	{
		return this.getClass().getName();
	}
	
	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}

}
