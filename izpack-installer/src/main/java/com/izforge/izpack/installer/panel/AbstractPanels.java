package com.izforge.izpack.installer.panel;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Variables;

/**
 * Abstract implementation of the {@link Panels} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPanels<T extends PanelView> implements Panels<T>
{

    /**
     * The panels.
     */
    private final List<T> panels;

    /**
     * The variables.
     */
    private final Variables variables;

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
     * @param panels    the panels
     * @param variables the variables. These are refreshed prior to each panel switch
     */
    public AbstractPanels(List<T> panels, Variables variables)
    {
        this.panels = panels;
        this.variables = variables;
        nextEnabled = !panels.isEmpty();
    }

    /**
     * Returns the panels.
     *
     * @return the panels
     */
    @Override
    public List<T> getPanels()
    {
        return panels;
    }

    /**
     * Returns the current panel.
     *
     * @return the current panel, or {@code null} if there is no current panel
     */
    @Override
    public T getPanel()
    {
        return getPanel(index);
    }

    /**
     * Determines if the current panel is valid.
     *
     * @return {@code true} if the current panel is valid
     */
    @Override
    public boolean isValid()
    {
        T panel = getPanel();
        return panel != null && executeValidationActions(panel, true);
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
     * Determines if there is another panel after the current panel.
     *
     * @return {@code true} if there is another panel
     */
    @Override
    public boolean hasNext()
    {
        return getNext(index, false) != -1;
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
     * <br/>
     * Navigation can only occur if the current panel is valid.
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
        T panel = getPanel();
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
        List<T> panels = getPanels();
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
    public int getVisibleIndex(T panel)
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

        // refresh variables prior to switching panels
        variables.refresh();

        T oldPanel = getPanel(index);
        T newPanel = getPanel(newIndex);
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
    protected abstract boolean switchPanel(T newPanel, T oldPanel);

    /**
     * Executes any pre and post-validation actions for a panel.
     *
     * @param panel    the panel
     * @param validate if {@code true}, validate the panel after executing the pre-validation actions
     * @return {@code true} if the panel is valid
     */
    protected boolean executeValidationActions(T panel, boolean validate)
    {
        panel.executePreValidationActions();
        boolean isValid = !validate || panel.isValid();
        panel.executePostValidationActions();
        return isValid;
    }

    /**
     * Returns the panel at the specified index.
     *
     * @param index the panel index
     * @return the corresponding panel, or {@code null} if there is no panel at the index
     */
    private T getPanel(int index)
    {
        List<T> panels = getPanels();
        return index >= 0 && index < panels.size() ? panels.get(index) : null;
    }

    /**
     * Determines if a panel can be shown.
     *
     * @param panel       the panel
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return {@code true} if the nominated panel can be shown
     */
    private boolean canShow(T panel, boolean visibleOnly)
    {
        return (!visibleOnly || panel.isVisible()) && panel.canShow();
    }

}
