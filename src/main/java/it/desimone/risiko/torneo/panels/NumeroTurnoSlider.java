package it.desimone.risiko.torneo.panels;

import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NumeroTurnoSlider extends JSlider {
	
	public NumeroTurnoSlider(){
		super(SwingConstants.HORIZONTAL,1,20,1);
		
        setMajorTickSpacing(19);
        setMinorTickSpacing(1);
		setPaintTicks(true);
		setPaintLabels(true);
		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
		toolTipManager.setInitialDelay(0);
		addMouseListener(toolTipManager);
		addMouseMotionListener(toolTipManager);
		addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				JSlider slider = (JSlider) arg0.getSource();
				slider.setToolTipText(String.valueOf(slider.getValue()));
			}
		});
	}

}
