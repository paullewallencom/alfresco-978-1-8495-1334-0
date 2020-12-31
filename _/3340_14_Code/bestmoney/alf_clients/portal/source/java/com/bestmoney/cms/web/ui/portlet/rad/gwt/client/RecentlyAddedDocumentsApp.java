package com.bestmoney.cms.web.ui.portlet.rad.gwt.client;

import com.bestmoney.cms.web.ui.portlet.rad.gwt.client.icons.AppIcons;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Recently added documents and folders GWT/GXT UI part,
 * producing the presentation for the portlet.
 *
 * @author martin.bergljung@opsera.com
 */
public class RecentlyAddedDocumentsApp implements EntryPoint {
    public static final AppIcons ICONS = GWT.create(AppIcons.class);

    public static final String HTML_DIV_ID = "recentlyAddedDocs";

    public static final String ROOT_JSON_PROPERTY = "content";
    public static final String LENGTH_JSON_PROPERTY = "contentLength";
    public static final String ID_PROPERTY = "id";
    public static final String LOCKED_PROPERTY = "locked";
    public static final String PATH_PROPERTY = "path";
    public static final String CHILDREN_SIZE_PROPERTY = "childrenSize";

    public static final String CM_NAME_PROPERTY = "cmName";
    public static final String CM_TITLE_PROPERTY = "cmTitle";
    public static final String CM_DESC_PROPERTY = "cmDescription";
    public static final String CM_MODIFIED_DATE_PROPERTY = "cmModified";
    public static final String CM_MODIFIER_PROPERTY = "cmModifier";

    public static final int MAX_ROWS_PER_PAGE = 25;

    public static final String DATETIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";

    public void onModuleLoad() {
        String url = "http://localhost:8080/alfresco/service/bestmoney/recent";
        ScriptTagProxy<PagingLoadResult<ModelData>> proxy = new ScriptTagProxy<PagingLoadResult<ModelData>>(url);

        ModelType type = getModelType();

        JsonPagingLoadResultReader<PagingLoadResult<ModelData>> reader =
                new JsonPagingLoadResultReader<PagingLoadResult<ModelData>>(type);

        final PagingLoader<PagingLoadResult<ModelData>> loader =
                new BasePagingLoader<PagingLoadResult<ModelData>>(proxy, reader);

        setupBeforeLoadListener(loader);
        loader.setSortDir(Style.SortDir.DESC);
        loader.setSortField(CM_NAME_PROPERTY);
        loader.setRemoteSort(true);

        // Create a store for the content
        final ListStore<ModelData> store = new ListStore<ModelData>(loader);

        // Create a grid to hold folder and document content
        Grid<ModelData> grid = createGrid(store, loader);

        // Create panel for content
        ContentPanel panel = createPanelForGrid(grid, loader);

        // Add everything to the div we specified in the HTML page
        RootPanel.get(HTML_DIV_ID).add(panel);
    }

    private ContentPanel createPanelForGrid(Grid<ModelData> grid,
                                            final PagingLoader<PagingLoadResult<ModelData>> loader) {
        ContentPanel panel = new ContentPanel();
        panel.setIcon(RecentlyAddedDocumentsApp.ICONS.documents());
        panel.setHeading("Folders and Documents");
        panel.setLayout(new FitLayout());
        panel.setBodyBorder(false);
        panel.setScrollMode(Style.Scroll.AUTO);
        panel.add(grid);

        // Make sure we can page through content
        final PagingToolBar toolBar = new PagingToolBar(MAX_ROWS_PER_PAGE);
        toolBar.bind(loader);
        panel.setBottomComponent(toolBar);

        return panel;
    }

    private Grid<ModelData> createGrid(ListStore<ModelData> store,
                                       final PagingLoader<PagingLoadResult<ModelData>> loader) {
        Grid<ModelData> grid = new Grid<ModelData>(store, getColumnModel());
        grid.setStateId("pagingContentGrid");
        grid.setStateful(true);
        grid.setLoadMask(true);
        grid.setBorders(false);
        grid.setStripeRows(true);
        grid.setAutoExpandColumn(CM_NAME_PROPERTY);
        grid.setAutoHeight(true);
        grid.setAutoWidth(true);
        grid.addListener(Events.Attach, new Listener<GridEvent<ModelData>>() {
            public void handleEvent(GridEvent<ModelData> ge) {
                loader.load(0, MAX_ROWS_PER_PAGE);
            }
        });

        // Render rows as they scroll into view
        BufferView view = new BufferView();
        view.setAutoFill(true);
        grid.setView(view);

        return grid;
    }

    /**
     * { "contentLength":"1",
     * "content":[
     * {
     * "locked":"false",
     * "cmName":"Alfresco_Getting_Started_Guide.pdf",
     * "cmTitle":"",
     * "cmDescription":"",
     * "cmModified":"05-Sep-2010 08:03:59",
     * "cmModifier":"admin",
     * "path":"/Company Home/docs/Alfresco",
     * "id":"2c712a49-28a8-448f-87e3-de95c5ff8d52",
     * "childrenSize":"0"
     * }
     * ]
     * }
     *
     * @return
     */
    private ModelType getModelType() {
        ModelType type = new ModelType();
        type.setRoot(ROOT_JSON_PROPERTY);
        type.setTotalName(LENGTH_JSON_PROPERTY);
        type.addField(LOCKED_PROPERTY);
        type.addField(CM_NAME_PROPERTY);
        type.addField(CM_TITLE_PROPERTY);
        type.addField(CM_DESC_PROPERTY);
        DataField datefield = new DataField(CM_MODIFIED_DATE_PROPERTY);
        datefield.setType(Date.class);
        datefield.setFormat(DATETIME_FORMAT);
        type.addField(datefield);
        type.addField(CM_MODIFIER_PROPERTY);
        type.addField(PATH_PROPERTY);
        type.addField(ID_PROPERTY);
        type.addField(CHILDREN_SIZE_PROPERTY);
        return type;
    }

    private void setupBeforeLoadListener(PagingLoader<PagingLoadResult<ModelData>> loader) {
        loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {
            public void handleEvent(LoadEvent loadEvent) {
                BasePagingLoadConfig loadConfig = loadEvent.<BasePagingLoadConfig>getConfig();
                loadConfig.set("alf_ticket", getAlfrescoTicket());
            }
        });
    }

    private ColumnModel getColumnModel() {
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        RowNumberer rn = new RowNumberer();
        rn.setWidth(30);
        columns.add(rn);

        ColumnConfig type = new ColumnConfig("Type", "Type", 35);
        GridCellRenderer<ModelData> typeRenderer = new GridCellRenderer<ModelData>() {
            public Object render(ModelData content, String property, ColumnData config,
                                 int rowIndex, int colIndex, ListStore<ModelData> store, Grid<ModelData> grid) {
                return GWTUtils.getImageForFileExtension((String) content.get(CM_NAME_PROPERTY)).getHTML();
            }
        };
        type.setRenderer(typeRenderer);
        columns.add(type);

        columns.add(new ColumnConfig(CM_NAME_PROPERTY, "Name", 125));

        ColumnConfig title = new ColumnConfig(CM_TITLE_PROPERTY, "Title", 100);
        title.setHidden(true);
        columns.add(title);

        ColumnConfig path = new ColumnConfig(PATH_PROPERTY, "Path", 150);
        columns.add(path);

        ColumnConfig desc = new ColumnConfig(CM_DESC_PROPERTY, "Description", 200);
        desc.setHidden(true);
        columns.add(desc);

        ColumnConfig date = new ColumnConfig(CM_MODIFIED_DATE_PROPERTY, "Modified", 100);
        date.setDateTimeFormat(DateTimeFormat.getFormat(DATETIME_FORMAT));
        columns.add(date);

        columns.add(new ColumnConfig(CM_MODIFIER_PROPERTY, "Modifier", 100));

        return new ColumnModel(columns);
    }

    public native String getAlfrescoTicket()
        /*-{
            return $wnd.getAlfrescoTicket();
        }-*/
            ;
}
