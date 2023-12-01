package org.telegram.ui.Charts

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Charts.data.ChartData
import org.telegram.ui.Charts.data.DoubleLinearChartData
import org.telegram.ui.Charts.view_data.ChartHorizontalLinesData
import org.telegram.ui.Charts.view_data.LineViewData


class DoubleLinearChartView(context: Context) : BaseChartView<DoubleLinearChartData, LineViewData>(context) {


   init {
       useMinHeight = true
       super.init()
   }


   override fun drawChart(canvas: Canvas) {
       if (chartData != null) {
           val fullWidth = chartWidth / (pickerDelegate.pickerEnd - pickerDelegate.pickerStart)
           val offset = fullWidth * pickerDelegate.pickerStart - HORIZONTAL_PADDING


           canvas.save()
           var transitionAlpha = 1f
           if (transitionMode == TRANSITION_MODE_PARENT) {
               transitionAlpha = if (transitionParams.progress > 0.5f) 0f else 1f - 2f * transitionParams.progress
               canvas.scale(1 + 2 * transitionParams.progress, 1f, transitionParams.pX, transitionParams.pY)
           } else if (transitionMode == TRANSITION_MODE_CHILD) {
               transitionAlpha = if (transitionParams.progress < 0.3f) 0f else transitionParams.progress
               canvas.save()
               canvas.scale(transitionParams.progress, transitionParams.progress, transitionParams.pX, transitionParams.pY)
           } else if (transitionMode == TRANSITION_MODE_ALPHA_ENTER) {
               transitionAlpha = transitionParams.progress
           }


           for (k in lines.indices) {
               val line = lines[k]
               if (!line.enabled && line.alpha == 0f) continue


               var j = 0
               val y = line.line.y


               line.chartPath.reset()
               var first = true


               val p = if (chartData.xPercentage.size < 2) 1f else chartData.xPercentage[1] * fullWidth
               val additionalPoints = (HORIZONTAL_PADDING / p).toInt() + 1
               val localStart = max(0, startXIndex - additionalPoints)
               val localEnd = min(chartData.xPercentage.size - 1, endXIndex + additionalPoints)


               for (i in localStart..localEnd) {
                   if (y[i] < 0) continue
                   val xPoint = chartData.xPercentage[i] * fullWidth - offset
                   val yPercentage =
                       (y[i].toFloat() * chartData.linesK[k] - currentMinHeight) / (currentMaxHeight - currentMinHeight)
                   val padding = line.paint.strokeWidth / 2f
                   val yPoint =
                       measuredHeight - chartBottom - padding - yPercentage * (measuredHeight - chartBottom - SIGNATURE_TEXT_HEIGHT - padding)


                   if (USE_LINES) {
                       if (j == 0) {
                           line.linesPath[j++] = xPoint
                           line.linesPath[j++] = yPoint
                       } else {
                           line.linesPath[j++] = xPoint
                           line.linesPath[j++] = yPoint
                           line.linesPath[j++] = xPoint
                           line.linesPath[j++] = yPoint
                       }
                   } else {
                       if (first) {
                           first = false
                           line.chartPath.moveTo(xPoint, yPoint)
                       } else {
                           line.chartPath.lineTo(xPoint, yPoint)
                       }
                   }
               }


               if (endXIndex - startXIndex > 100) {
                   line.paint.strokeCap = Paint.Cap.SQUARE
               } else {
                   line.paint.strokeCap = Paint.Cap.ROUND
               }
               line.paint.alpha = (255 * line.alpha * transitionAlpha).toInt()
               if (!USE_LINES) canvas.drawPath(line.chartPath, line.paint)
               else canvas.drawLines(line.linesPath, 0, j, line.paint)
           }


           canvas.restore()
       }
   }


   override fun drawPickerChart(canvas: Canvas) {
       val bottom = measuredHeight - PICKER_PADDING
       val top = measuredHeight - pikerHeight - PICKER_PADDING


       val nl = lines.size
       if (chartData != null) {
           for (k in 0 until nl) {
               val line = lines[k]
               if (!line.enabled && line.alpha == 0f) continue


               line.bottomLinePath.reset()


               val n = chartData.xPercentage.size
               var j = 0


               val y = line.line.y


               line.chartPath.reset()
               for (i in 0 until n) {
                   if (y[i] < 0) continue


                   val xPoint = chartData.xPercentage[i] * pickerWidth
                   val h = if (ANIMATE_PICKER_SIZES) pickerMaxHeight else chartData.maxValue.toFloat()


                   val yPercentage = y[i].toFloat() * chartData.linesK[k] / h
                   val yPoint = (1f - yPercentage) * (bottom - top)


                   if (USE_LINES) {
                       if (j == 0) {
                           line.linesPathBottom[j++] = xPoint
                           line.linesPathBottom[j++] = yPoint
                       } else {
                           line.linesPathBottom[j++] = xPoint
                           line.linesPathBottom[j++] = yPoint
                           line.linesPathBottom[j++] = xPoint
                           line.linesPathBottom[j++] = yPoint
                       }
                   } else {
                       if (i == 0) {
                           line.bottomLinePath.moveTo(xPoint, yPoint)
                       } else {
                           line.bottomLinePath.lineTo(xPoint, yPoint)
                       }
                   }
               }


               line.linesPathBottomSize = j


               if (!line.enabled && line.alpha == 0f) continue
               line.bottomLinePaint.alpha = (255 * line.alpha).toInt()
               if (USE_LINES)
                   canvas.drawLines(line.linesPathBottom, 0, line.linesPathBottomSize, line.bottomLinePaint)
               else
                   canvas.drawPath(line.bottomLinePath, line.bottomLinePaint)
           }
       }
   }


   override fun drawSelection(canvas: Canvas) {
       if (selectedIndex < 0 || !legendShowing) return


       val alpha = (chartActiveLineAlpha * selectionA).toInt()


       val fullWidth = chartWidth / (pickerDelegate.pickerEnd - pickerDelegate.pickerStart)
       val offset = fullWidth * pickerDelegate.pickerStart - HORIZONTAL_PADDING


       val xPoint = chartData.xPercentage[selectedIndex] * fullWidth - offset


       selectedLinePaint.alpha = alpha
       canvas.drawLine(xPoint, 0f, xPoint, chartArea.bottom.toFloat(), selectedLinePaint)


       var tmpN = lines.size
       for (tmpI in 0 until tmpN) {
           val line = lines[tmpI]
           if (!line.enabled && line.alpha == 0f) continue
           val yPercentage =
               ((line.line.y[selectedIndex].toFloat() * chartData.linesK[tmpI] - currentMinHeight) / (currentMaxHeight - currentMinHeight))
           val yPoint = measuredHeight - chartBottom - yPercentage * (measuredHeight - chartBottom - SIGNATURE_TEXT_HEIGHT)


           line.selectionPaint.alpha = (255 * line.alpha * selectionA).toInt()
           selectionBackgroundPaint.alpha = (255 * line.alpha * selectionA).toInt()


           canvas.drawPoint(xPoint, yPoint, line.selectionPaint)
           canvas.drawPoint(xPoint, yPoint, selectionBackgroundPaint)
       }
   }


   override fun drawSignaturesToHorizontalLines(canvas: Canvas, a: ChartHorizontalLinesData) {
       val n = a.values.size
       val rightIndex = if (chartData.linesK[0] == 1) 1 else 0
       val leftIndex = (rightIndex + 1) % 2


       var additionalOutAlpha = 1f
       if (n > 2) {
           val v = (a.values[1] - a.values[0]) / (currentMaxHeight - currentMinHeight).toFloat()
           if (v < 0.1) {
               additionalOutAlpha = v / 0.1f
           }
       }


       var transitionAlpha = 1f
       when (transitionMode) {
           TRANSITION_MODE_PARENT -> transitionAlpha = 1f - transitionParams.progress
           TRANSITION_MODE_CHILD -> transitionAlpha = transitionParams.progress
           TRANSITION_MODE_ALPHA_ENTER -> transitionAlpha = transitionParams.progress
       }


       linePaint.alpha = (a.alpha * 0.1f * transitionAlpha).toInt()
       val chartHeight = measuredHeight - chartBottom - SIGNATURE_TEXT_HEIGHT


       val textOffset = (SIGNATURE_TEXT_HEIGHT - signaturePaint.textSize).toInt()
       for (i in 0 until n) {
           val y = (measuredHeight - chartBottom - chartHeight * ((a.values[i] - currentMinHeight) / (currentMaxHeight - currentMinHeight))).toInt()
           if (a.valuesStr != null && lines.size > 0) {
               if (a.valuesStr2 == null || lines.size < 2) {
                   signaturePaint.color = Theme.getColor(Theme.key_statisticChartSignature)
                   signaturePaint.alpha = (a.alpha * signaturePaintAlpha * transitionAlpha * additionalOutAlpha).toInt()
               } else {
                   signaturePaint.color = lines[leftIndex].lineColor
                   signaturePaint.alpha = (a.alpha * lines[leftIndex].alpha * transitionAlpha * additionalOutAlpha).toInt()
               }


               canvas.drawText(a.valuesStr[i], HORIZONTAL_PADDING, (y - textOffset).toFloat(), signaturePaint)
           }
           if (a.valuesStr2 != null && lines.size > 1) {
               signaturePaint2.color = lines[rightIndex].lineColor
               signaturePaint2.alpha = (a.alpha * lines[rightIndex].alpha * transitionAlpha * additionalOutAlpha).toInt()
               canvas.drawText(a.valuesStr2[i], (measuredWidth - HORIZONTAL_PADDING).toFloat(), (y - textOffset).toFloat(), signaturePaint2)
           }
       }
   }


   override fun createLineViewData(line: ChartData.Line): LineViewData {
       return LineViewData(line)
   }


   fun findMaxValue(startXIndex: Int, endXIndex: Int): Int {
       if (lines.isEmpty()) {
           return 0
       }
       val n = lines.size
       var max = 0
       for (i in 0 until n) {
           val localMax =
               if (lines[i].enabled) (chartData.lines[i].segmentTree.rMaxQ(startXIndex, endXIndex) * chartData.linesK[i]).toInt() else 0
           if (localMax > max) max = localMax
       }
       return max
   }


   fun findMinValue(startXIndex: Int, endXIndex: Int): Int {
       if (lines.isEmpty()) {
           return 0
       }
       val n = lines.size
       var min = Int.MAX_VALUE
       for (i in 0 until n) {
           val localMin =
               if (lines[i].enabled) (chartData.lines[i].segmentTree.rMinQ(startXIndex, endXIndex) * chartData.linesK[i]).toInt() else Int.MAX_VALUE
           if (localMin < min) min = localMin
       }
       return min
   }


   protected fun updatePickerMinMaxHeight() {
       if (!ANIMATE_PICKER_SIZES) return
       if (lines[0].enabled) {
           super.updatePickerMinMaxHeight()
           return
       }


       var max = 0
       for (l in lines) {
           if (l.enabled && l.line.maxValue > max) max = l.line.maxValue
       }
       if (lines.size > 1) {
           max = (max * chartData.linesK[1]).toInt()
       }


       if (max > 0 && max != animatedToPickerMaxHeight) {
           animatedToPickerMaxHeight = max
           if (pickerAnimator != null) pickerAnimator.cancel()


           pickerAnimator = createAnimator(pickerMaxHeight, animatedToPickerMaxHeight.toFloat(), ValueAnimator.AnimatorUpdateListener {
               pickerMaxHeight = it.animatedValue as Float
               invalidatePickerChart = true
               invalidate()
           })
           pickerAnimator!!.start()
       }
   }


   protected fun createHorizontalLinesData(newMaxHeight: Int, newMinHeight: Int): ChartHorizontalLinesData {
       val k = if (chartData.linesK.size < 2) 1f else {
           val rightIndex = if (chartData.linesK[0] == 1) 1 else 0
           chartData.linesK[rightIndex]
       }
       return ChartHorizontalLinesData(newMaxHeight, newMinHeight, useMinHeight, k)
   }
}
