<template>
  <Row :gutter="24">
    <Col class="ivu-mb" span="6">
    <Card :bordered="false" dis-hover :padding="12">
      <p slot="title">
        <span>{{ $t('dashboard.allCustomers') }}</span>
      </p>
      <Tag slot="extra" color="green">{{ $t('dashboard.all') }}</Tag>
      <div>
        <div class="number">{{statisticsList.all}}</div>
        <Divider style="margin: 8px 0" />
        <div class="ivu-pt-8" style="height: 22px;">
          {{ $t('dashboard.totalCustomers') }}<span class="renshu">{{statisticsList.all}}</span>{{ $t('dashboard.people') }}
        </div>
      </div>
    </Card>
    </Col>
    <Col class="ivu-mb" span="6">
    <Card :bordered="false" dis-hover :padding="12">
      <p slot="title">
        <span>{{ $t('dashboard.newCustomers') }}</span>
      </p>
      <Tag slot="extra" color="green">{{ $t('dashboard.today') }}</Tag>
      <div>
        <div class="number">{{statisticsList.toDayKefu}}</div>
        <Divider style="margin: 8px 0" />
        <div class="ivu-pt-8" style="height: 22px;">
          {{ $t('dashboard.todayNewCustomers') }}<span class="renshu">{{statisticsList.toDayKefu}}</span>{{ $t('dashboard.people') }}
        </div>

      </div>
    </Card>
    </Col>
    <Col class="ivu-mb" span="6">
    <Card :bordered="false" dis-hover :padding="12">
      <p slot="title">
        <span>{{ $t('dashboard.newVisitors') }}</span>
      </p>
      <Tag slot="extra" color="green">{{ $t('dashboard.today') }}</Tag>
      <div>
        <div class="number">{{statisticsList.toDayTourist}}</div>
        <Divider style="margin: 8px 0" />
        <div class="ivu-pt-8" style="height: 22px;">
          {{ $t('dashboard.todayNewVisitors') }}<span class="renshu">{{statisticsList.toDayTourist}}</span>{{ $t('dashboard.people') }}
        </div>

      </div>
    </Card>
    </Col>
    <Col class="ivu-mb" span="6">
    <Card :bordered="false" dis-hover :padding="12">
      <p slot="title">
        <span>{{ $t('dashboard.newCustomers') }}</span>
      </p>
      <Tag slot="extra" color="green">{{ $t('dashboard.thisMonth') }}</Tag>
      <div>
        <div class="number">{{statisticsList.month}}</div>
        <Divider style="margin: 8px 0" />
        <div class="ivu-pt-8" style="height: 22px;">
          {{ $t('dashboard.monthNewCustomers') }}<span class="renshu">{{statisticsList.month}}</span>{{ $t('dashboard.people') }}
        </div>

      </div>
    </Card>
    </Col>
  </Row>
  <!--<div class="statistics">-->
    <!--<ul class="statistics-ul">-->
      <!--<li>-->
        <!--<div class="text">全部客户</div>-->
        <!--<div class="number-li">-->
          <!--<countTo :startVal='0' :endVal='statisticsList.all' :duration='durations'></countTo>-->
          <!--<span>人</span>-->
        <!--</div>-->
      <!--</li>-->
      <!--<li>-->
        <!--<div class="text">今日新增客户</div>-->
        <!--<div class="number-li">-->
          <!--<countTo :startVal='0' :endVal='statisticsList.toDayKefu' :duration='durations'></countTo>-->
          <!--<span>人</span>-->
        <!--</div>-->
      <!--</li>-->
      <!--<li>-->
        <!--<div class="text">本月新增客户</div>-->
        <!--<div class="number-li">-->
          <!--<countTo :startVal='0' :endVal='statisticsList.month' :duration='durations'></countTo>-->
          <!--<span>人</span>-->
        <!--</div>-->
      <!--</li>-->
      <!--<li>-->
        <!--<div class="text">今日游客</div>-->
        <!--<div class="number-li">-->
          <!--<countTo :startVal='0' :endVal='statisticsList.toDayTourist' :duration='durations'></countTo>-->
          <!--<span>人</span>-->
        <!--</div>-->
      <!--</li>-->
    <!--</ul>-->
  <!--</div>-->
</template>

<script>
import { sumApi } from "@/api/index";
import countTo from 'vue-count-to';
export default {
  name: "statistics",
  components: { countTo },
  data() {
    return {
      durations: 3000,
      statisticsList: {}
    }
  },
  methods: {
    getStatistics() {
      sumApi().then(async res => {
        let da = res.data
        this.statisticsList = da;
        console.log(da);
      }).catch(res => {
        this.$Message.error(res.msg)
      })
    }
  },
  mounted() {
    this.getStatistics()
  }
}
</script>

<style lang="less">
  .number{
    font-size: 30px;
    margin-bottom: 10px;
  }
  .renshu{
    color: red;
    padding: 0 3px;
  }
.statistics {
  width: 100%;
  margin: 20px 0;
  .statistics-ul {
    width: 100%;
    list-style: none;
    display: flex;
    li {
      width: 24%;
      list-style: none;
      padding: 28px 8px;
      margin-right: 2%;
      background-color: #ffffff;
      .text {
        color: #777777;
        font-size: 14px;
        padding-bottom: 6px;
      }
      .number-li {
        color: #282828;
        font-size: 19px;
        font-weight: 600;
        span {
          font-size: 13px;
        }
        span:first-of-type {
          font-size: 19px;
        }
      }
    }
    li:last-of-type {
      margin-right: 0;
    }
  }
}
</style>
