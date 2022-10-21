package com.stooit.betterconsole;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleFactory;

public class MyConsoleViewConsoleFactory implements IConsoleFactory {

	int counter = 1;

	@Override
	public void openConsole() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			final IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				try {
					final String secondaryId = "Console View #" + counter; //$NON-NLS-1$
					page.showView("com.stooit.betterconsole.MyConsole", secondaryId, 1);
					counter++;
				} catch (final PartInitException e) {
					ConsolePlugin.log(e);
				}
			}
		}
	}

}