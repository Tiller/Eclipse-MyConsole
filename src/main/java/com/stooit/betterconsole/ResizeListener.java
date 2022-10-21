package com.stooit.betterconsole;

import java.awt.MouseInfo;
import java.awt.Point;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ResizeListener implements Listener {
	private final Control widget;
	private final GridData data;
	
	private Point mousePoint;

	public ResizeListener(final Control widget, final GridData data) {
		this.widget = widget;
		this.data = data;
	}
	
	public void handleEvent(final Event e) {
		final Point newMousePoint = MouseInfo.getPointerInfo().getLocation();

		if (e.type == SWT.MouseDown) {
			mousePoint = newMousePoint;
		} else if (e.type == SWT.MouseMove && mousePoint != null) {
			final int moved = newMousePoint.x - mousePoint.x;
			data.widthHint += moved;
			widget.setLayoutData(data);
			widget.getParent().layout();
			mousePoint = newMousePoint;
		} else if (e.type == SWT.MouseUp) {
			mousePoint = null;
		}
	}
}
