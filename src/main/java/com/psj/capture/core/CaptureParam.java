package com.psj.capture.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 抓图参数
 *
 * @author Administrator
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CaptureParam {

    /**
     * 预置点编号
     */
    private int presetIndex;

    /**
     * 抓图绝对路径
     */
    private String filePath;


}
