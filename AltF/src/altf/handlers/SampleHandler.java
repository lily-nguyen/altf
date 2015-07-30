package altf.handlers;

import java.util.HashSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
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

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {
	
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
	
	
	/**
	 * The constructor.
	 */
	public SampleHandler() {
	}

	
	public IEditorPart getActiveEditor(IWorkbenchWindow window) {
		IWorkbenchPage activePage= window.getActivePage();
		if (activePage == null)
			return null;

		IEditorPart activeEditor= activePage.getActiveEditor();
		return activeEditor;

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
		// getContainer().getActiveEditorInput().getAdapter(IFile.class)
		Object o = getActiveEditorInput(window).getAdapter(IFile.class);
		if (o == null) {
			System.out.println("getAdapter is null");
		} else {
			System.out.println(o.toString());
		}
		return o;
	}
	
	
	public FileTextSearchScope createTextSearchScope(IWorkbenchWindow window) {
		String[] filter = new String[]{"*"};
//		return FileTextSearchScope.newWorkspaceScope(filter, false);
		HashSet resources= new HashSet();
		resources.add(getFileResource(window));
		IResource[] arr= (IResource[]) resources.toArray(new IResource[resources.size()]);
		return FileTextSearchScope.newSearchScope(arr, filter, false);
	}	
	
	
	private ISearchQuery newQuery(String textPattern, IWorkbenchWindow window) throws CoreException {
		TextSearchPageInput input = null;
		ISearchQuery query = null;
		input= new TextSearchPageInput(textPattern, false, false, false, createTextSearchScope(window));
		//TextSearchQueryProvider.getPreferred() not null;
		query = TextSearchQueryProvider.getPreferred().createQuery(input);
		return query;
	}

	
	private String getText(ISelection selection) {
		
		// TODO if selection is not instace of ITextSelection, return empty
		String textPattern= "";
		textPattern = ((ITextSelection) selection).getText();

		return textPattern;
		
	}
	
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try{
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			String textPattern = getText(window.getSelectionService().getSelection());
			System.out.println("textPattern for search is: " + textPattern);
	//		NewSearchUI.openSearchDialog(window, "org.eclipse.search.internal.ui.text.TextSearchPage");
			ISearchQuery query = newQuery(textPattern, window);
			if (query == null) {
				System.out.println("nulllllllllllllllllll");
			} else {
				NewSearchUI.runQueryInBackground(query);
			}
		} catch (Exception e) {
			// do nothing
		}
		return null;
	}
	
	
}
