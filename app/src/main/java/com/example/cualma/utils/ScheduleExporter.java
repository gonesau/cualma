package com.example.cualma.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.LruCache;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import java.io.OutputStream;

public class ScheduleExporter {

    // Método para capturar el RecyclerView completo como Bitmap
    public static Bitmap captureRecyclerView(RecyclerView view) {
        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;

        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            Paint paint = new Paint();

            // Calculamos el tamaño de la caché (1/8 de la memoria disponible)
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;

            // Instanciamos LruCache correctamente
            LruCache<String, Bitmap> bitmapCache = new LruCache<>(cacheSize);

            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {
                    // Guardamos una copia del bitmap en la caché
                    bitmapCache.put(String.valueOf(i), Bitmap.createBitmap(drawingCache));
                }
                height += holder.itemView.getMeasuredHeight();
                holder.itemView.setDrawingCacheEnabled(false); // Limpiamos
            }

            // Creamos el bitmap gigante
            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height + 50, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            bigCanvas.drawColor(Color.WHITE); // Fondo blanco

            // Título opcional
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            bigCanvas.drawText("Mi Horario - CualMa", 20, 40, paint);

            int currentHeight = 50; // Margen superior para el título
            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmapCache.get(String.valueOf(i));
                if (bitmap != null) {
                    bigCanvas.drawBitmap(bitmap, 0, currentHeight, paint);
                    currentHeight += bitmap.getHeight();
                    // No reciclamos aquí inmediatamente si planeamos reusar,
                    // pero para exportar está bien dejar que el GC lo maneje o limpiar la caché al final.
                }
            }
        }
        return bigBitmap;
    }

    // Método para guardar el Bitmap en la URI seleccionada por el usuario
    public static void saveBitmapToUri(Context context, Bitmap bitmap, Uri uri) {
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
                Toast.makeText(context, "Horario guardado correctamente", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}