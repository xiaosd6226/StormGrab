package com.neptune.bolt.analyze;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.google.gson.Gson;
import com.neptune.config.facerig.PictureKey;
import com.neptune.constant.LogPath;
import com.neptune.util.HDFSHelper;
import com.neptune.util.LogWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by neptune on 16-9-18.
 * 将人脸文件从HDFS上下载的bolt
 */
public class DownloadBolt extends BaseRichBolt {
    private static String LOG_PATH = "/download-bolt.log";
    private static final String TAG = "dowload-bolt";

    private OutputCollector collector;
    private TopologyContext context;
    private int id;

    private HDFSHelper hdfs;

    public DownloadBolt() {
        super();
        LOG_PATH = LogPath.APATH + "/download-bolt.log";
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        context = topologyContext;
        collector = outputCollector;
        id = context.getThisTaskId();
        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        String json = tuple.getString(0);
        Gson gson = new Gson();
        PictureKey key = gson.fromJson(json, PictureKey.class);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        hdfs = new HDFSHelper(key.dir);
        if (hdfs.download(os, key.url)) {
            try {
                //读取图片
                ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
                BufferedImage img = ImageIO.read(in);
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                //压缩缓冲区

            } catch (IOException e) {
                LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": " + e.getMessage());
            }
        } else {
            LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": fail to download :" + key.url);
        }
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
