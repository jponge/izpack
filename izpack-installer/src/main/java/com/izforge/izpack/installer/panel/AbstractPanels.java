package com.izforge.izpack.installer.panel;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.handler.AbstractUIHandler;

/**
 * Abstract implementation of the {@link Panels} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPanels<T> implements Panels<T>
{

    /**
     * The panels.
     */
    private final List<PanelView<T>> panels;

    /**
     * The current panel index.
     */
    private int index = -1;

    /**
     * Determines if the next panel may be navigated to, if any.
     */
    private boolean nextEnabled;

    /**
     * Determines if the previous panel may be navigated to, if any.
     */
    private boolean previousEnabled;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AbstractPanels.class.getName());

    /**
     * Constructs an {@code AbstractPanels}.
     *
     * @param panels the panels
     */
    public AbstractPanels(List<PanelView<T>> panels)
    {
        this.panels = panels;
        nextEnabled = !panels.isEmpty();
    }

    /**
     * Returns the panels.
     *
     * @return the panels
     */
    @Override
    public List<PanelView<T>> getPanels()
    {
        return panels;
    }

    /**
     * Returns the current panel.
     *
     * @return the current panel, or {@code null} if there is no current panel
     */
    @Override
    public PanelView<T> getPanel()
    {
        return getPanel(index);
    }

    /**
     * Returns the current panel index.
     *
     * @return the current panel index, or {@code -1} if there is no current panel
     */
    @Override
    public int getIndex()
    {
        return index;
    }

    /**
     * Determines if the next panel may be navigated to.
     *
     * @param enable if {@code true}, enable navigation, otherwise disable it
     */
    @Override
    public void setNextEnabled(boolean enable)
    {
        nextEnabled = enable;
    }

    /**
     * Determines if navigation to the next panel has been enabled.
     * <p/>
     * return {@code true} if navigation is enabled
     */
    @Override
    public boolean isNextEnabled()
    {
        return nextEnabled;
    }

    /**
     * Navigates to the next panel.
     *
     * @return {@code true} if the next panel was navigated to
     */
    @Override
    public boolean next()
    {
        return next(true);
    }

    /**
     * Navigates to the next panel.
     *
     * @param validate if {@code true}, only move to the next panel if validation succeeds
     * @return {@code true} if the next panel was navigated to
     */
    @Override
    public boolean next(boolean validate)
    {
        boolean result = false;
        PanelView<T> panel = getPanel();
        boolean isValid = panel == null || executeValidationActions(panel, validate);
        if (isValid && isNextEnabled())     // NOTE: actions may change isNextEnabled() status
        {
            int newIndex = getNext(index, false);
            if (newIndex != -1)
            {
                result = switchPanel(newIndex);
            }
        }
        return result;
    }

    /**
     * Determines if the previous panel may be navigated to.
     *
     * @param enable if {@code true}, enable navigation, otherwise disable it
     */
    @Override
    public void setPreviousEnabled(boolean enable)
    {
        previousEnabled = enable;
    }

    /**
     * Determines if navigation to the previous panel has been enabled.
     * <p/>
     * return {@code true} if navigation is enabled
     */
    @Override
    public boolean isPreviousEnabled()
    {
        return previousEnabled;
    }

    /**
     * Navigates to the previous panel.
     *
     * @return {@code true} if the previous panel was navigated to
     */
    @Override
    public boolean previous()
    {
        return previous(index);
    }

    /**
     * Navigates to the panel before the specified index.
     * <br/>
     * The target panel must be before the current panel.
     *
     * @return {@code true} if the previous panel was navigated to
     */
    @Override
    public boolean previous(int index)
    {
        boolean result = false;
        if (isPreviousEnabled())
        {
            int newIndex = getPrevious(index, true);
            if (newIndex != -1)
            {
                result = switchPanel(newIndex);
            }
        }
        return result;
    }

    /**
     * Determines if there is another panel after the specified index.
     *
     * @param index       the panel index
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return {@code true} if there is another panel
     */
    @Override
    public int getNext(int index, boolean visibleOnly)
    {
        int result = -1;
        List<PanelView<T>> panels = getPanels();
        for (int i = index + 1; i < panels.size(); ++i)
        {
            if (canShow(panels.get(i), visibleOnly))
            {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Determines if there is another panel prior to the specified index.
     *
     * @param index       the panel index
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return the previous panel index, or {@code -1} if there are no more panels
     */
    @Override
    public int getPrevious(int index, boolean visibleOnly)
    {
        int result = -1;
        for (int i = index - 1; i >= 0; --i)
        {
            if (canShow(getPanel(i), visibleOnly))
            {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the index of a visible panel, relative to other visible panels.
     *
     * @param panel the panel
     * @return the panel's visible index, or {@code -1} if the panel is not visible
     */
    @Override
    public int getVisibleIndex(PanelView<T> panel)
    {
        int result = 0;
        if (panel.isVisible())
        {
            for (int i = 0; i <= panel.getIndex() && i < panels.size(); ++i)
            {
                if (panels.get(i).isVisible())
                {
                    result++;
                }
            }
        }
        else
        {
            result = -1;
        }
        return result;
    }

    /**
     * Returns the number of visible panels.
     *
     * @return the number of visible panels
     */
    @Override
    public int getVisible()
    {
        int result = 0;
        for (PanelView panel : panels)
        {
            if (panel.isVisible())
            {
                ++result;
            }
        }
        return result;
    }

    /**
     * Switches panels.
     *
     * @param newIndex the index of the new panel
     * @return {@code true} if the switch was successful
     */
    protected boolean switchPanel(int newIndex)
    {
        boolean result;
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Selecting panel=" + newIndex + ", old index=" + index);
        }
        PanelView<T> oldPanel = getPanel(index);
        PanelView<T> newPanel = getPanel(newIndex);
        if (switchPanel(newPanel, oldPanel))
        {
            index = newIndex;
            result = true;
        }
        else
        {
            result = false;
        }
        return result;
    }

    /**
     * Switches panels.
     *
     * @param newPanel the panel to switch to
     * @param oldPanel the panel to switch from, or {@code null} if there was no prior panel
     * @return {@code true} if the switch was successful
     */
    protected abstract boolean switchPanel(PanelView<T> newPanel, PanelView<T> oldPanel);

    /**
     * Executes any pre and post-validation actions for a panel.
     *
     * @param panel    the panel
     * @param validate if {@code true}, validate the panel after executing the pre-validation actions
     * @return {@code true} if the panel is valid
     */
    protected boolean executeValidationActions(PanelView<T> panel, boolean validate)
    {
        AbstractUIHandler handler = getHandler(panel);
        panel.executePreValidationActions(handler);
        boolean isValid = !validate || isValid(panel);
        panel.executePostValidationActions(handler);
        return isValid;
    }

    /**
     * Determines if a panel is valid.
     *
     * @param panel the panel to check
     * @return {@code true} if the panel is valid
     */
    protected abstract boolean isValid(PanelView<T> panel);

    /**
     * Returns a handler to pass to a panel's actions.
     *
     * @param panel the panel
     * @return the handler to use
     */
    protected abstract AbstractUIHandler getHandler(PanelView<T> panel);

    /**
     * Returns the panel at the specified index.
     *
     * @param index the panel index
     * @return the corresponding panel, or {@code null} if there is no panel at the index
     */
    private PanelView<T> getPanel(int index)
    {
        List<PanelView<T>> panels = getPanels();
        return index >= 0 && index < panels.size() ? panels.get(index) : null;
    }

    /**
     * Determines if a panel can be shown.
     *
     * @param panel       the panel
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return {@code true} if the nominated panel can be shown
     */
    private boolean canShow(PanelView<T> panel, boolean visibleOnly)
    {
        return (!visibleOnly || panel.isVisible()) && panel.canShow();
    }

}
