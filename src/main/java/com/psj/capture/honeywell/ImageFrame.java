package com.psj.capture.honeywell;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Administrator
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class ImageFrame {

    private byte[] yv12;
    private int width;
    private int height;

    private int yStride;
    private int vStride;
    private int uStride;

}
