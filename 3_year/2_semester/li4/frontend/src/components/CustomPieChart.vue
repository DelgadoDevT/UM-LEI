<template>
  <div ref="chart"></div>
</template>

<script setup>
import { onMounted, ref, watchEffect } from "vue";
import * as d3 from "d3";

const props = defineProps({
  data: Array,
  height: Number,
  width: Number,
  labels: Boolean,
  animations: {
    type: Boolean,
  },
  palette: Array
});

const chart = ref(null);

const renderChart = () => {
  d3.select(chart.value).select("svg").remove();

  const svg = d3
    .select(chart.value)
    .append("svg")
    .attr("width", props.width)
    .attr("height", props.height)
    .append("g")
    .attr("transform", `translate(${props.width / 2}, ${props.height / 2})`);

  const radius = Math.min(props.width, props.height) / 2 * 0.6; // Adjust radius for label space

  const pie = d3
    .pie()
    .sort(null)
    .value((d) => d.value);

  const sectors = pie(props.data);
  const total = d3.sum(props.data, d => d.value);

  // Arc for the pie slices
  const arc = d3
    .arc()
    .outerRadius(radius)
    .innerRadius(0);

  // Arc for positioning the labels
  const labelArc = d3.arc()
    .outerRadius(radius + 40) // Increased distance
    .innerRadius(radius + 40);

  const paths = svg
    .selectAll(".arc")
    .data(sectors)
    .enter()
    .append("path")
    .attr("fill", (d, i) => props.palette[i % props.palette.length])
    .attr("d", arc);

  if (props.animations) {
    paths
      .transition()
      .duration(1000)
      .attrTween("d", function (d) {
        const i = d3.interpolate(d.startAngle + 0.1, d.endAngle);
        return function (t) {
          d.endAngle = i(t);
          return arc(d);
        };
      });
  }

  if (props.labels) {
    const labelGroup = svg.append("g").attr("class", "labels");

    labelGroup.selectAll('text')
      .data(sectors)
      .enter()
      .append('text')
      .text(d => `${((d.value / total) * 100).toFixed(0)}% ${d.data.name}`)
      .attr('transform', d => `translate(${labelArc.centroid(d)})`)
      .style('text-anchor', 'middle')
      .style('fill', (d, i) => props.palette[i % props.palette.length])
      .attr('class', 'text-xs font-bold');
  }
};

onMounted(renderChart);
watchEffect(renderChart);
</script>
