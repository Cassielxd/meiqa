<template>
  <div class="chart-content">
    <div class="chart-title">
      <p class="chart-left">{{ $t('dashboard.trendChart') }}</p>
      <p class="chart-right">
        <RadioGroup v-model="visitDate" type="button" class="ivu-mr-8" @on-change="handleChangeVisitType">
          <Radio label="1">{{ $t('dashboard.year') }}</Radio>
          <Radio label="0">{{ $t('dashboard.month') }}</Radio>
        </RadioGroup>
      </p>
    </div>
    <div class="">
      <div id="echarts1" style="width:100%;height: 400px;"></div>
    </div>
  </div>
</template>
<script>

import echarts from "echarts";
import { charApi } from "../../../api";
export default {
  name: "statisticsCharts",
  data() {
    return {
      option: {},
      year: '2021',
      month: '07',
      visitDate: '0'
    }
  },
  methods: {
    getChart() {
      this.option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: [this.$t('dashboard.customers'), this.$t('dashboard.visitors')],
          icon: 'rect',
          right: 20,
          top: 20
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          nameTextStyle: {
            color: '#CCCCCC'
          },
          axisLine: {
            lineStyle: {
              color: '#CCCCCC'
            }
          },
          axisLabel: {
            color: '#666666',

          },
          data: []
        },
        yAxis: {
          type: 'value',
          name: this.$t('dashboard.dailyNewPeople'),
          nameTextStyle: {
            color: '#CCCCCC'
          },
          axisLine: {
            lineStyle: {
              color: '#CCCCCC'
            }
          },
          axisLabel: {
            color: '#666666',

          },
          splitLine: {
            lineStyle: {
              type: 'dashed'
            }
          }
        },
        series: [
          {
            name: this.$t('dashboard.customers'),
            type: 'line',
            itemStyle: {
              normal: {
                color: '#1890FF'
              }
            },
            lineStyle: {
              width: 3
            },
            data: []
          },
          {
            name: this.$t('dashboard.visitors'),
            type: 'line',
            itemStyle: {
              normal: {
                color: '#10CCA3'
              }
            },
            lineStyle: {
              width: 3
            },
            data: []
          }
        ]
      };
      var data = {
        year: this.year,
        month: this.month
      }
      charApi(data).then(async res => {
        let da = res.data
        let keArr = da.list
        let youArr = da.tourist
        let seriesKe = []
        let seriesYou = []
        let xAxis = []
        keArr.forEach((val, index) => {
          xAxis.push(val.month)
          if(youArr[index]){
            seriesYou.push(youArr[index].number)
          }
          seriesKe.push(val.number)
        })
        //console.log(seriesKe)
        this.option.xAxis.data = xAxis
        this.option.series[0].data = seriesKe
        this.option.series[1].data = seriesYou
        let myChart = echarts.init(document.getElementById('echarts1'))
        // 基于准备好的dom，初始化echarts实例
        myChart.setOption(this.option, true);
        window.onresize = myChart.resize()
      }).catch(res => {
        this.$Message.error(res.msg)
      })
    },
    handleChangeVisitType() {

      charApi({ type: this.visitDate }).then(res => {
        let da = res.data
        let keArr = da.list
        let youArr = da.tourist
        let seriesKe = []
        let seriesYou = []
        let xAxis = []
        keArr.forEach((val, index) => {
          xAxis.push(val.month)
          seriesYou.push(youArr[index].number)
          seriesKe.push(val.number)
        })
        //console.log(seriesKe)
        this.option.xAxis.data = xAxis
        this.option.series[0].data = seriesKe
        this.option.series[1].data = seriesYou
        let myChart = echarts.init(document.getElementById('echarts1'))
        // 基于准备好的dom，初始化echarts实例
        myChart.setOption(this.option, true);
        window.onresize = myChart.resize();
      }).catch(rej => {
        this.$Message.error(rej.msg)
      })

    }
  },
  mounted() {
    this.getChart()
  }
}
</script>

<style lang="less" scoped>
.chart-content {
  width: 100%;
  height: auto;
  background-color: #ffffff;
  .chart-title {
    border-bottom: 2px solid #f5f5f5;
    overflow: hidden;
    padding: 10px 10px;
    .chart-left {
      float: left;
      color: #000000;
      font-size: 16px;
      font-weight: 800;
    }
    .chart-right {
      float: right;
      cursor: pointer;
      span {
        display: inline-block;
        border: 2px solid #1890ff;
        color: #1890ff;
        padding: 2px 16px;
      }
      span:last-of-type {
        border-left: none;
      }
      span.active {
        background-color: #1890ff;
        color: #ffffff;
      }
    }
  }
}
</style>
