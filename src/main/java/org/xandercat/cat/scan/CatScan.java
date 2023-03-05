package org.xandercat.cat.scan;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.scan.swing.FileSearchFrame;
import org.xandercat.swing.util.PlatformTool;

/**
 * CatScan is a file searching utility.
 * 
 * @author Scott Arnold
 */
public class CatScan {

	private static final Logger log = LogManager.getLogger(CatScan.class);
	private static final String APPLICATION_NAME = "CatScan";
	private static final String APPLICATION_VERSION = "1.1";
	
	public static void main(String[] args) {
		log.info(APPLICATION_NAME + " " + APPLICATION_VERSION);
		if (!PlatformTool.isMac()) {
			for (LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(lafInfo.getName())) {
					try {
						UIManager.setLookAndFeel(lafInfo.getClassName());
					} catch (Exception e) {
						log.error("Unable to activate Nimbus Look And Feel.  Using default.", e);
					}
					break;
				}
			}
		}
		PlatformTool.setApplicationNameOnMac(APPLICATION_NAME);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				launchUI();
			}
		});
	}
	
	private static void launchUI() {
		FileSearchFrame frame = new FileSearchFrame(APPLICATION_NAME, APPLICATION_VERSION);
		frame.setVisible(true);
	}
}
