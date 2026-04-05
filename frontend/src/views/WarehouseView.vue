<script setup>
import { onMounted, reactive, ref } from "vue";
import { createWarehouse, fetchWarehouses } from "../api/grain";

const warehouses = ref([]);
const form = reactive({
  code: "",
  name: "",
  location: "",
  capacityTon: 500,
  managerName: "",
  status: "RUNNING"
});

async function loadWarehouses() {
  warehouses.value = await fetchWarehouses();
}

async function submit() {
  await createWarehouse(form);
  form.code = "";
  form.name = "";
  form.location = "";
  form.capacityTon = 500;
  form.managerName = "";
  form.status = "RUNNING";
  await loadWarehouses();
}

onMounted(loadWarehouses);
</script>

<template>
  <div class="stack">
    <div class="panel">
      <div class="section-title">新增仓库</div>
      <div class="form-grid two-columns">
        <label><span>编码</span><input v-model="form.code" class="input" /></label>
        <label><span>名称</span><input v-model="form.name" class="input" /></label>
        <label><span>位置</span><input v-model="form.location" class="input" /></label>
        <label><span>容量(吨)</span><input v-model="form.capacityTon" class="input" type="number" /></label>
        <label><span>负责人</span><input v-model="form.managerName" class="input" /></label>
        <label>
          <span>状态</span>
          <select v-model="form.status" class="input">
            <option value="RUNNING">运行中</option>
            <option value="MAINTAINING">维护中</option>
          </select>
        </label>
      </div>
      <button class="primary-btn" @click="submit">保存仓库</button>
    </div>

    <div class="panel">
      <div class="section-title">仓库列表</div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>编码</th>
            <th>名称</th>
            <th>位置</th>
            <th>容量</th>
            <th>负责人</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in warehouses" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.code }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.location }}</td>
            <td>{{ item.capacityTon }}</td>
            <td>{{ item.managerName }}</td>
            <td>{{ item.status }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
