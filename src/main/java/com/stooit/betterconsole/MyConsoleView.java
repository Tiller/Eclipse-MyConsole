package com.stooit.betterconsole;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.internal.console.ConsoleView;

@SuppressWarnings("restriction")
public class MyConsoleView extends ConsoleView {

	private final Set<IConsole> dirtyConsoles = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final Map<IConsole, IDocumentListener> documentListeners = new ConcurrentHashMap<>();

	private List listWidget;
	
	@Override
	public void createPartControl(final Composite parent) {
		final GridLayout parentLayout = new GridLayout();
		parentLayout.numColumns = 3;
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parentLayout.horizontalSpacing = 1;
		parent.setLayout(parentLayout);

		listWidget = new List(parent, SWT.NONE);
		final GridData listLayoutData = new GridData(GridData.FILL_VERTICAL);
		listLayoutData.widthHint = 150;
		listWidget.setLayoutData(listLayoutData);
		listWidget.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		listWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				display(getConsoles().get(listWidget.getSelectionIndex()));
			}
		});
		final Menu menuWidget = new Menu(listWidget);
		listWidget.setMenu(menuWidget);
		final MenuItem removeConsoleItem = new MenuItem(menuWidget, SWT.NONE);
		removeConsoleItem.setText("Close console");

		removeConsoleItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (listWidget.getSelectionIndex() < 0) {
					return;
				}

				ConsolePlugin.getDefault().getConsoleManager()
						.removeConsoles(new IConsole[] { getConsoles().get(listWidget.getSelectionIndex()) });
			}
		});

		final Composite resizeBarWidget = new Composite(parent, SWT.NONE);
		final GridData dataResize = new GridData(GridData.FILL_VERTICAL);
		dataResize.widthHint = 2;
		resizeBarWidget.setLayoutData(dataResize);
		resizeBarWidget.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_SIZEWE));
		resizeBarWidget.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_FOREGROUND));

		final Listener listener = new ResizeListener(listWidget, listLayoutData);
		resizeBarWidget.addListener(SWT.MouseDown, listener);
		resizeBarWidget.addListener(SWT.MouseMove, listener);
		resizeBarWidget.addListener(SWT.MouseUp, listener);

		super.createPartControl(parent);

		getPageBook().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	private void refresh() {
		if (isAvailable()) {
			asyncExec(this::doRefresh);
		}
	}

	private void doRefresh() {
		if (isAvailable()) {
			listWidget.removeAll();

			final java.util.List<IConsole> consoles = getConsoles();
			for (final IConsole console : consoles) {
				if (console instanceof TextConsole && !documentListeners.containsKey(console)) {
					final IDocumentListener listener = new IDocumentListener() {
						@Override
						public void documentChanged(final DocumentEvent event) {
							markDirty(console);
						}

						@Override
						public void documentAboutToBeChanged(final DocumentEvent event) {
							// do nothing
						}
					};

					documentListeners.put(console, listener);

					((TextConsole) console).getDocument().addDocumentListener(listener);
				}
				if (dirtyConsoles.contains(console)) {
					listWidget.add("* " + console.getName());
				} else {
					listWidget.add(console.getName());
				}
			}

			final Iterator<Entry<IConsole, IDocumentListener>> it = documentListeners.entrySet().iterator();
			while (it.hasNext()) {
				final Entry<IConsole, IDocumentListener> next = it.next();

				if (!consoles.contains(next.getKey())) {
					((TextConsole) next.getKey()).getDocument().removeDocumentListener(next.getValue());
					it.remove();
				}
			}

			listWidget.select(consoles.indexOf(getConsole()));
		}
	}

	private void markDirty(final IConsole console) {
		if (console != getConsole()) {
			dirtyConsoles.add(console);
			refresh();
		}
	}

	@Override
	public void consolesAdded(final IConsole[] consoles) {
		super.consolesAdded(consoles);
		refresh();
	}

	@Override
	public void consolesRemoved(final IConsole[] consoles) {
		super.consolesRemoved(consoles);
		refresh();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		super.propertyChange(event);
		refresh();
	}

	@Override
	public void display(final IConsole console) {
		super.display(console);

		dirtyConsoles.remove(console);
		refresh();
	}

	@Override
	public void warnOfContentChange(final IConsole console) {
		super.warnOfContentChange(console);

		markDirty(console);
	}

	private boolean isAvailable() {
		return getPageBook() != null && !getPageBook().isDisposed();
	}

	private static java.util.List<IConsole> getConsoles() {
		return Arrays.asList(ConsolePlugin.getDefault().getConsoleManager().getConsoles());
	}
}
