<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="流程名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入流程名" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="所属流程" prop="processDefinitionId">
        <el-input v-model="queryParams.processDefinitionId" placeholder="请输入流程定义的编号" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="流程分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择流程分类" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.BPM_MODEL_CATEGORY)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="提交时间">
        <el-date-picker v-model="dateRangeCreateTime" size="small" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="结果" prop="result">
        <el-select v-model="queryParams.result" placeholder="请选择流结果" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">发起流程</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" width="320" />
      <el-table-column label="流程名" align="center" prop="name" />
      <el-table-column label="流程分类" align="center" prop="category">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.BPM_MODEL_CATEGORY, scope.row.category) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="当前审批任务" align="center" prop="tasks">
        <template slot-scope="scope">
          <el-button v-for="task in scope.row.tasks" type="text" @click="handleFormDetail(task.id)">
            <span>{{ task.name }}</span>
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>
            <el-tag type="primary" v-if="scope.row.status === 1"> <!-- 进行中 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS, scope.row.status) }}
            </el-tag>
             <el-tag type="success" v-if="scope.row.status === 2"> <!-- 已结束 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS, scope.row.status) }}
            </el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="结果" align="center" prop="result">
        <template slot-scope="scope">
          <span>
            <el-tag type="primary" v-if="scope.row.result === 1"> <!-- 进行中 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT, scope.row.result) }}
            </el-tag>
             <el-tag type="success" v-if="scope.row.result === 2"> <!-- 通过 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT, scope.row.result) }}
            </el-tag>
             <el-tag type="danger" v-if="scope.row.result === 3"> <!-- 不通过 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT, scope.row.result) }}
            </el-tag>
             <el-tag type="info" v-if="scope.row.result === 4"> <!-- 撤回 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT, scope.row.result) }}
            </el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <!-- TODO 芋艿：权限 -->
          <el-button type="text" size="small" icon="el-icon-delete" v-if="scope.row.result === 1"
                     @click="handleCancel(scope.row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

  </div>
</template>

<script>
import {
  getMyProcessInstancePage,
  createProcessInstanceExt,
  updateProcessInstanceExt,
  deleteProcessInstanceExt,
  getProcessInstanceExt,
  getProcessInstanceExtPage,
  exportProcessInstanceExtExcel, cancelProcessInstance
} from "@/api/bpm/processInstance";
import {deleteErrorCode} from "@/api/system/errorCode";

export default {
  name: "ProcessInstanceExt",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 工作流的流程实例的拓展列表
      list: [],
      // 是否显示弹出层
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        processDefinitionId: null,
        category: null,
        status: null,
        result: null,
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 处理查询参数
      let params = {...this.queryParams};
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行查询
      getMyProcessInstancePage(params).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 **/
    handleAdd() {
      this.$router.push({ path: "/bpm/process-instance/create"})
    },
    /** 取消按钮操作 */
    handleCancel(row) {
      const id = row.id;
      this.$prompt('请输出取消原因？', "取消流程", {
        type: 'warning',
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputPattern: /^[\s\S]*.*[^\s][\s\S]*$/, // 判断非空，且非空格
        inputErrorMessage: "取消原因不能为空",
      }).then(({ value }) => {
        return cancelProcessInstance(id, value);
      }).then(() => {
        this.getList();
        this.msgSuccess("取消成功");
      })
    },
  }
};
</script>
