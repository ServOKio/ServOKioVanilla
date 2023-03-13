package net.servokio.vanilla.modules.mods.lockScreenWidget;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;

public class WidgetHost extends AppWidgetHost {
    public WidgetHost(Context context, int hostId) {
        super(context, hostId);
    }

    @Override // android.appwidget.AppWidgetHost
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        return new LSWidgetHostView(context);
    }

    @Override // android.appwidget.AppWidgetHost
    protected void clearViews() {
    }
}
