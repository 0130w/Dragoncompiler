import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Instances of this class allows multiple listeners to receive events
 * while walking the parse tree. For example:
 *
 * <pre>
 * ProxyParseTreeListener proxy = new ProxyParseTreeListener();
 * ParseTreeListener listener1 = ... ;
 * ParseTreeListener listener2 = ... ;
 * proxy.add( listener1 );
 * proxy.add( listener2 );
 * ParseTreeWalker.DEFAULT.walk( proxy, ctx );
 * </pre>
 */
public class ProxyParseTreeListener implements ParseTreeListener {
    private List<ParseTreeListener> listeners;

    /**
     * Creates a new proxy without an empty list of listeners. Add
     * listeners before walking the tree.
     */
    public ProxyParseTreeListener() {
        // Setting the listener to null automatically instantiates a new list.
        this( null );
    }

    /**
     * Creates a new proxy with the given list of listeners.
     *
     * @param listeners A list of listerners to receive events.
     */
    public ProxyParseTreeListener( List<ParseTreeListener> listeners ) {
        setListeners( listeners );
    }

    @Override
    public void enterEveryRule( ParserRuleContext ctx ) {
        Iterator<ParseTreeListener> i = iterator();

        while( i.hasNext() ) {
            ParseTreeListener listener = i.next();
            listener.enterEveryRule( ctx );
            ctx.enterRule( listener );
        }
    }

    @Override
    public void exitEveryRule( ParserRuleContext ctx ) {
        Iterator<ParseTreeListener> i = iterator();

        while( i.hasNext() ) {
            ParseTreeListener listener = i.next();
            ctx.exitRule( listener );
            listener.exitEveryRule( ctx );
        }
    }

    @Override
    public void visitErrorNode( ErrorNode node ) {
        Iterator<ParseTreeListener> i = iterator();

        while( i.hasNext() ) {
            ParseTreeListener listener = i.next();
            listener.visitErrorNode( node );
        }
    }

    @Override
    public void visitTerminal( TerminalNode node ) {
        Iterator<ParseTreeListener> i = iterator();

        while( i.hasNext() ) {
            ParseTreeListener listener = i.next();
            listener.visitTerminal( node );
        }
    }

    /**
     * Adds the given listener to the list of event notification recipients.
     *
     * @param listener A listener to begin receiving events.
     */
    public void add( ParseTreeListener listener ) {
        getListeners().add( listener );
    }

    /**
     * Removes the given listener to the list of event notification recipients.
     *
     * @param listener A listener to stop receiving events.
     * @return false The listener was not registered to receive events.
     */
    public boolean remove( ParseTreeListener listener ) {
        return getListeners().remove( listener );
    }

    /**
     * Returns an iterator of a copy of the current list. This protects
     * against concurrent modifications to the list.
     *
     * @return A non-null, possibly empty, list of ParseTreeListeners that
     * will receive events.
     */
    private Iterator<ParseTreeListener> iterator() {
        List<ParseTreeListener> list = createParseTreeListenerList();
        list.addAll( getListeners() );
        return list.iterator();
    }

    /**
     * Returns the list of listeners.
     *
     * @return The list of listeners to receive tree walking events.
     */
    private List<ParseTreeListener> getListeners() {
        return this.listeners;
    }

    /**
     * Changes the list of listeners to receive events. If the given list of
     * listeners is null, an empty list will be created.
     *
     * @param listeners A list of listeners to receive tree walking
     * events.
     */
    public void setListeners( List<ParseTreeListener> listeners ) {
        if( listeners == null ) {
            listeners = createParseTreeListenerList();
        }

        this.listeners = listeners;
    }

    protected List<ParseTreeListener> createParseTreeListenerList() {
        return new ArrayList<ParseTreeListener>();
    }
}