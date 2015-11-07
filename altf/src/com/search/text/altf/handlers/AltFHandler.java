package com.search.text.altf.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.eclipse.search.ui.text.TextSearchQueryProvider.TextSearchInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AltFHandler extends AbstractHandler {

	private static class TextSearchPageInput extends TextSearchInput {

		private final String fSearchText;
		private final boolean fIsCaseSensitive;
		private final boolean fIsRegEx;
		private final boolean fIsWholeWord;
		private final FileTextSearchScope fScope;

		public TextSearchPageInput(String searchText, boolean isCaseSensitive, boolean isRegEx, boolean isWholeWord, FileTextSearchScope scope) {
			fSearchText= searchText;
			fIsCaseSensitive= isCaseSensitive;
			fIsRegEx= isRegEx;
			fIsWholeWord= isWholeWord;
			fScope= scope;
		}

		public String getSearchText() {
			return fSearchText;
		}

		public boolean isCaseSensitiveSearch() {
			return fIsCaseSensitive;
		}

		public boolean isRegExSearch() {
			return fIsRegEx;
		}

		public boolean isWholeWordSearch() {
			return fIsWholeWord;
		}

		public FileTextSearchScope getScope() {
			return fScope;
		}
	}

	
	public AltFHandler() {
		super();
	}
	

	public IEditorPart getActiveEditor(IWorkbenchWindow window) {

		IWorkbenchPage activePage= window.getActivePage();
		if (activePage == null)
			return null;

		IWorkbenchPart activePart= activePage.getActivePart();
		if (activePart == null)
			return null;

		IEditorPart activeEditor= activePage.getActiveEditor();
		if (activeEditor == activePart || isOldSearchView(activePart))
			return activeEditor;

		return null;

	}
	
	
	private static boolean isOldSearchView(IWorkbenchPart part) {
		return org.eclipse.search.ui.SearchUI.SEARCH_RESULT_VIEW_ID.equals(part.getSite().getId());
	}
	
	
	public IEditorInput getActiveEditorInput(IWorkbenchWindow window) {
		IEditorPart editor= getActiveEditor(window);
		if (editor == null)
			return null;

		// Handle multi-page editors
		if (editor instanceof MultiPageEditorPart) {
			Object page= ((MultiPageEditorPart)editor).getSelectedPage();
			if (page instanceof IEditorPart)
				editor= (IEditorPart)page;
			else
				return null;
		}

		return editor.getEditorInput();
	}	
	
	
	public Object getFileResource(IWorkbenchWindow window) {
		
		IEditorInput eInput = getActiveEditorInput(window);
		if (eInput == null) {
			return null;
		}
		return getActiveEditorInput(window).getAdapter(IFile.class);
	}
	
	
	public FileTextSearchScope createTextSearchScope(IWorkbenchWindow window) {
		
		String[] filter = new String[]{"*"};
		
		HashSet resources= new HashSet();
		
		Object o = getFileResource(window);
		
		if (o != null) {
			resources.add(getFileResource(window));
		}
		
		IResource[] arr= (IResource[]) resources.toArray(new IResource[resources.size()]);
		
		return FileTextSearchScope.newSearchScope(arr, filter, false);
	}	
	
	
	private ISearchQuery newQuery(String textPattern, IWorkbenchWindow window) throws CoreException {
		
		TextSearchPageInput input = null;
		ISearchQuery query = null;
		input= new TextSearchPageInput(textPattern, true, false, true, createTextSearchScope(window));
		query = TextSearchQueryProvider.getPreferred().createQuery(input);
		return query;
	}

	
	private String insertEscapeChars(String text) {
		if (text == null || text.equals("")) //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		StringBuffer sbIn= new StringBuffer(text);
		BufferedReader reader= new BufferedReader(new StringReader(text));
		int lengthOfFirstLine= 0;
		try {
			lengthOfFirstLine= reader.readLine().length();
		} catch (IOException ex) {
			return ""; //$NON-NLS-1$
		}
		StringBuffer sbOut= new StringBuffer(lengthOfFirstLine + 5);
		int i= 0;
		while (i < lengthOfFirstLine) {
			char ch= sbIn.charAt(i);
			if (ch == '*' || ch == '?' || ch == '\\')
				sbOut.append("\\"); //$NON-NLS-1$
			sbOut.append(ch);
			i++;
		}
		return sbOut.toString();
	}

	
	private String getText(ITextSelection selection) {
		
		String textPattern= "";
		textPattern = insertEscapeChars(selection.getText());

		return textPattern;
		
	}
	
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try{
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			
			ISelection selection = window.getSelectionService().getSelection();
			if (selection instanceof ITextSelection) {
				String textPattern = getText((ITextSelection)selection);
//				System.out.println("text pattern is [" + textPattern + "]");
				
				ISearchQuery query = newQuery(textPattern, window);
				NewSearchUI.runQueryInBackground(query);
			} 
//			else {
//				System.out.println("out of scope");
//			}
		} catch (Exception e) {
			// do nothing
		}
		return null;
	}
}
