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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
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


/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql script to be executed)
 *  
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntrySendToS3Dialog extends JobEntryDialog implements JobEntryDialogInterface
{
	static S3Bucket[] myBuckets;
	static S3Service s3Service;
	
    private Label        wlName;
    private Text         wName;
    private FormData     fdlName, fdName;
  
    private Label        wlAccessKey;
    public static TextVar      wAccessKey;
    private FormData     fdlAccessKey, fdAccessKey;
    
	private Label        wlPrivateKey;
	public static TextVar      wPrivateKey;
	private FormData     fdlPrivateKey, fdPrivateKey;
	
	private Label        wlS3Bucket;
	private TextVar      wS3Bucket;
	private FormData     fdlS3Bucket, fdS3Bucket;
	
	private Label        wlFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdFilename;
	
	private Button wOK, wCancel, wBuckManage;
	private Listener lsOK, lsCancel, lsBuckMgmt;

	private JobEntrySendToS3     jobEntry;
	private Shell       	shell, shell2;
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
		
		
		//Buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");
		wBuckManage=new Button(shell, SWT.PUSH);
		wBuckManage.setText(" &Manage Buckets ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel, wBuckManage }, margin, wFilename);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e){ cancel(); 	}};
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsBuckMgmt = new Listener() { public void handleEvent(Event e) { BuckMgmt();     } };
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		wBuckManage.addListener    (SWT.Selection, lsBuckMgmt    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
	
        wAccessKey.addSelectionListener( lsDef );
        wPrivateKey.addSelectionListener( lsDef );
        wS3Bucket.addSelectionListener( lsDef );
        wFilename.addSelectionListener(lsDef);
        			
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
				
		getData();
		
		BaseStepDialog.setSize(shell);
		shell.pack();
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
	
	public void disposeBuckMgmt()
	{
		WindowProperty winprop = new WindowProperty(shell2);
		props.setScreen(winprop);
		shell2.dispose();
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
	
	public void BuckMgmt()
	{
		// Bucket Management screen
		// Display
		Shell parent = shell;
		final Display display2 = parent.getDisplay();
		shell2 = new Shell (parent);
		shell2.setSize(450, 400);
		JobDialog.setShellImage(shell2, jobEntry);
		shell2.setText("Bucket management");
		//Label Bucket number
		final Label label0 = new Label (shell2, SWT.NONE);
		label0.setText("");
		label0.setBounds(10,40,250,20);
		//List
		
		final Table BucketTable = new Table(shell2, SWT.BORDER);
		BucketTable.setBounds(10, 60, 420, 200);
		BucketTable.setLinesVisible(true);
		BucketTable.setHeaderVisible(true);

	    TableColumn BucketCol = new TableColumn(BucketTable, SWT.NONE);
	    BucketCol.setText("Bucket");
	    BucketCol.setWidth(200);
	    TableColumn BucketDate = new TableColumn(BucketTable, SWT.NONE);
	    BucketDate.setText("Date Created");
	    BucketDate.setWidth(175);
	    TableColumn BucketObj = new TableColumn(BucketTable, SWT.NONE);
	    BucketObj.setText("Nb Objects");
	    BucketObj.setWidth(40);
	    
        for (int i=0; i<4; i++)
        {
        	TableItem item = new TableItem(BucketTable, SWT.NONE);
        	item.setText(new String[] {""});
        }
     	//Bucket list Button
		Button ConnectToS3 = new Button (shell2, SWT.PUSH);
		ConnectToS3.setText ("List buckets");
		ConnectToS3.setBounds(175, 10, 100, 30);
		ConnectToS3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					
					if((wAccessKey.getText().isEmpty()) || (wPrivateKey.getText().isEmpty()))
					{
						label0.setText("Please fill in the Access key and Secret key first !!!");
						return;
					}
					AWSConnect();
					BucketTable.removeAll();
			        for (int i=0; i<myBuckets.length; i++)
			        {
			        	TableItem item = new TableItem(BucketTable, SWT.NONE);
			        	S3Object[] objects = s3Service.listObjects(myBuckets[i]);
			        	item.setText(new String[] { myBuckets[i].getName(), myBuckets[i].getCreationDate().toString(), Integer.toString(objects.length)});
			    		
			        }
			        label0.setText("You have " + myBuckets.length + " Buckets in Amazon S3");
				} catch (S3ServiceException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				}
			}
		});
		//Label Bucket creation
		final Label label2 = new Label (shell2, SWT.NONE);
		label2.setText("Bucket creation");
		label2.setBounds(270,270,200,20);
		//Text new bucket
		final Text text2 = new Text(shell2, SWT.BORDER);
		text2.setText("");
		text2.setBounds(270,290,160,20);
		text2.setTextLimit(30);
		//Label Bucket created
		final Label label1 = new Label (shell2, SWT.NONE);
		label1.setText("");
		label1.setBounds(270,310,200,20);
		//Create Bucket Button
		Button CBucketInS3 = new Button (shell2, SWT.PUSH);
		CBucketInS3.setText ("Create Bucket");
		CBucketInS3.setBounds(270, 330, 100, 30);
		CBucketInS3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//Create bucket routine here
				S3Bucket Bucket2Create;
				try {
					Bucket2Create = s3Service.createBucket(text2.getText());
					label1.setText("Bucket successfully created");
					myBuckets = s3Service.listAllBuckets();

					BucketTable.removeAll();
			        for (int i=0; i<myBuckets.length; i++)
			        {
			        	TableItem item = new TableItem(BucketTable, SWT.NONE);
			        	S3Object[] objects = s3Service.listObjects(myBuckets[i]);
			        	item.setText(new String[] { myBuckets[i].getName(), myBuckets[i].getCreationDate().toString(), Integer.toString(objects.length)});
			        }
			        label0.setText("You have " + myBuckets.length + " Buckets in Amazon S3");					
					
				} catch (S3ServiceException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		//Label Target Bucket
		final Label label3 = new Label (shell2, SWT.NONE);
		label3.setText("Target bucket : ");
		label3.setBounds(10,270,200,20);
		//Text for Access Key
		final Text text1 = new Text(shell2, SWT.BORDER);
		text1.setText("");
		text1.setBounds(10,290,200,20);
		text1.setTextLimit(30);
		//OK Button
		Button OKBut = new Button (shell2, SWT.PUSH);
		OKBut.setText ("&Ok");
		OKBut.setBounds(10, 330, 100, 30);
		OKBut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				wS3Bucket.setText(text1.getText().toString());
				disposeBuckMgmt();
			}
		});
		//CANCEL Button
		Button CancelBut = new Button (shell2, SWT.PUSH);
		CancelBut.setText ("&Cancel");
		CancelBut.setBounds(120, 330, 100, 30);
		CancelBut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				disposeBuckMgmt();
			}
		});

		//listeners
		BucketTable.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				TableItem[] Selct = BucketTable.getSelection();
				text1.setText (Selct[0].getText());
			}
		});

		//shell.pack ();		
		shell2.open ();
		while (!shell2.isDisposed ()) {
			if (!display2.readAndDispatch ()) display2.sleep ();
		}
		//disposeBuckMgmt ();
	}
	private static void AWSConnect() throws S3ServiceException {
		AWSCredentials awsCredentials = new AWSCredentials(wAccessKey.getText().toString(), wPrivateKey.getText().toString());
		s3Service = new RestS3Service(awsCredentials);
		myBuckets = s3Service.listAllBuckets();
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
