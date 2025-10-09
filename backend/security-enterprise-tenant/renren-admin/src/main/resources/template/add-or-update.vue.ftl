<#assign editor=false/>
<template>
  <el-dialog v-model="visible" :title="!dataForm.${pk.attrName} ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
<#list columnList as column>
  <#if column.form>
    <#if column.formType == 'text'>
      <el-form-item label="${column.comment!}" prop="${column.attrName}">
        <el-input v-model="dataForm.${column.attrName}" placeholder="${column.comment!}"></el-input>
      </el-form-item>
    <#elseif column.formType == 'textarea'>
      <el-form-item label="${column.comment!}" prop="${column.attrName}">
        <el-input type="textarea" v-model="dataForm.${column.attrName}"></el-input>
      </el-form-item>
    <#elseif column.formType == 'editor'>
      <el-form-item label="${column.comment!}" prop="${column.attrName}" style="height: 400px">
        <WangEditor v-model="dataForm.content"></WangEditor>
      </el-form-item>
      <#assign editor=true/>
      <#assign editorName="${column.attrName}"/>
    <#elseif column.formType == 'select'>
      <#if column.dictName??>
      <el-form-item label="${column.comment!}" prop="${column.attrName}">
        <ren-select v-model="dataForm.${column.attrName}" dict-type="${column.dictName}" placeholder="${column.comment!}"></ren-select>
      </el-form-item>
      <#else>
      <el-form-item label="${column.comment!}" prop="${column.attrName}">
        <el-select v-model="dataForm.${column.attrName}" placeholder="请选择">
          <el-option label="人人" value="0"></el-option>
        </el-select>
      </el-form-item>
      </#if>
    <#elseif column.formType == 'radio'>
      <#if column.dictName??>
      <el-form-item label="${column.comment!}" prop="${column.attrName}">
        <ren-radio-group v-model="dataForm.${column.attrName}" dict-type="${column.dictName}"></ren-radio-group>
      </el-form-item>
      <#else>
      <el-form-item label="${column.comment!}" prop="${column.attrName}">
        <el-radio-group v-model="dataForm.${column.attrName}">
          <el-radio :label="0">启用</el-radio>
          <el-radio :label="1">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      </#if>
    <#elseif column.formType == 'checkbox'>
      <el-form-item label="${column.comment!}" prop="${column.attrName}">
        <el-checkbox-group v-model="dataForm.${column.attrName}">
          <el-checkbox label="启用" name="type"></el-checkbox>
          <el-checkbox label="禁用" name="type"></el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    <#elseif column.formType == 'date'>
      <el-form-item label="${column.comment!}" prop="${column.attrName}">
        <el-date-picker type="date" placeholder="${column.comment!}" v-model="dataForm.${column.attrName}" format="YYYY-MM-DD" value-format="YYYY-MM-DD"></el-date-picker>
      </el-form-item>
    <#elseif column.formType == 'datetime'>
      <el-form-item label="${column.comment!}" prop="${column.attrName}">
        <el-date-picker type="datetime" placeholder="${column.comment!}" v-model="dataForm.${column.attrName}" format="YYYY-MM-DD hh:mm:ss" value-format="YYYY-MM-DD hh:mm:ss"></el-date-picker>
      </el-form-item>
    <#else>
      <el-form-item label="${column.comment!}" prop="${column.attrName}">
        <el-input v-model="dataForm.${column.attrName}" placeholder="${column.comment!}"></el-input>
      </el-form-item>
    </#if>
  </#if>
</#list>
    </el-form>
    <template v-slot:footer>
      <el-button @click="visible = false">{{ $t("cancel") }}</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">{{ $t("confirm") }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import baseService from "@/service/baseService";
<#if editor>
import WangEditor from "@/components/wang-editor/index.vue";
</#if>
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  <#list columnList as column>
  ${column.attrName}: "",
  </#list>
});

const rules = ref({
  <#list columnList as column>
  <#if column.form && column.required>
  ${column.attrName}: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  </#if>
  </#list>
});

const init = (${pk.attrName}?: number) => {
  visible.value = true;
  dataForm.${pk.attrName} = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (${pk.attrName}) {
    getInfo(${pk.attrName});
  }
};

// 获取信息
const getInfo = (${pk.attrName}: number) => {
  baseService.get("/${moduleName}/${classname}/" + ${pk.attrName}).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.${pk.attrName} ? baseService.post : baseService.put)("/${moduleName}/${classname}", dataForm).then((res) => {
      ElMessage.success({
        message: t("prompt.success"),
        duration: 500,
        onClose: () => {
          visible.value = false;
          emit("refreshDataList");
        }
      });
    });
  });
};

defineExpose({
  init
});
</script>